import asyncio
import json
import argparse
import time
import websockets
from networktables import NetworkTables

DEFAULT_ROBORIO = "10.91.63.2"
WS_HOST = "0.0.0.0"
DEFAULT_WS_PORT = 5810
WS_PATH = "/nt/dashboard"

POLL_INTERVAL = 0.02
PULSE_TIME = 0.05 

clients = set()

TABLES_AND_KEYS = {
    "RobotStress": [
        "batteryVoltage",
        "totalCurrent",
        "drivetrainCurrent",
        "stressScore",
        "stressLevel",
        "speedScale",
        "chassisSpeed"
    ],
    "StreamDeck/IntakeAngle": [
        "toggleCount",
        "calibrateZero",
        "calibrateTarget"
    ],
    "StreamDeck/IntakeRoller": [
        "intakeToggle",
        "outtakeToggle"
    ],
    "limelight-back": [
        "piece_tx",
        "ta",
        "piece_distance",
        "has_target",
        "bbox",
        "hw"
    ],
    "limelight-front": [
        "tx",
        "tv",
        "ta",
        "hw"
    ],
    "Modes": [
        "AimLockLime4",
        "AimLockLime2",
        "AlignLime2"
    ]
}

# ================= NETWORKTABLES =================

def connect_nt(roborio_host):
    """Conecta ao NetworkTables do RoboRIO"""
    print(f"🔗 Inicializando NT -> {roborio_host}")
    NetworkTables.initialize(server=roborio_host)

    waited = 0.0
    while not NetworkTables.isConnected():
        time.sleep(0.1)
        waited += 0.1
        if waited > 10.0:
            print("❌ Timeout NT - RoboRIO não respondeu em 10s")
            break

    if NetworkTables.isConnected():
        print("✅ NT conectado com sucesso!")
    else:
        print("⚠️ Aviso: NT não conectado na inicialização")


async def monitor_nt_connection(roborio_host):
    """
    Monitora a conexão com NetworkTables e reconecta automaticamente
    se a conexão cair
    """
    print("📡 Monitor de conexão NT iniciado")
    
    while True:
        try:
            if not NetworkTables.isConnected():
                print("⚠️ NT desconectado! Tentando reconectar...")
                NetworkTables.initialize(server=roborio_host)
                
                waited = 0.0
                while not NetworkTables.isConnected() and waited < 10.0:
                    await asyncio.sleep(0.5)
                    waited += 0.5
                
                if NetworkTables.isConnected():
                    print("✅ NT reconectado com sucesso!")
                else:
                    print("❌ Falha ao reconectar NT")
            
            await asyncio.sleep(5.0)  # Verifica a cada 5 segundos
            
        except Exception as e:
            print(f"❌ Erro no monitor NT: {type(e).__name__}: {e}")
            await asyncio.sleep(5.0)


def get_table(name):
    """Obtém uma tabela do NetworkTables"""
    try:
        return NetworkTables.getTable(name)
    except Exception as e:
        print(f"❌ Erro ao obter tabela '{name}': {e}")
        return None


def read_any(table, key):
    """
    Lê qualquer tipo de valor da tabela
    Tenta Number, Boolean, String e Arrays
    """
    if table is None:
        return None
        
    for fn in (
        table.getNumberArray,
        table.getNumber,
        table.getBoolean,
        table.getString
    ):
        try:
            v = fn(key, None)
            if v is not None:
                result = list(v) if isinstance(v, (list, tuple)) else v
                return result
        except Exception as e:
            # Silencia erros esperados (tipo incorreto)
            pass
    
    return None


def is_valid_table_key(table_name, key):
    """Valida se tabela e chave existem na configuração"""
    if table_name not in TABLES_AND_KEYS:
        return False
    return key in TABLES_AND_KEYS[table_name]


# ================= BUTTON PULSE =================

async def pulse_button(table, key_name, delay):
    """
    Executa um pulse de botão de forma confiável
    Seta TRUE, aguarda, depois seta FALSE
    """
    try:
        if table is None:
            print(f"❌ Tabela None ao fazer pulse de {key_name}")
            return
            
        table.putBoolean(key_name, True)
        print(f"📤 PULSE START: {key_name}")
        
        await asyncio.sleep(delay)
        
        table.putBoolean(key_name, False)
        print(f"✅ PULSE END: {key_name}")
        
    except Exception as e:
        print(f"❌ Erro no pulse: {type(e).__name__}: {e}")


# ================= POLL LOOP =================

async def poll_and_broadcast():
    """
    Lê dados do NetworkTables e envia para clientes WebSocket
    Apenas envia se o valor mudou (otimização)
    """
    last = {}
    error_count = 0
    max_errors = 10

    while True:
        try:
            for table_name, keys in TABLES_AND_KEYS.items():
                table = get_table(table_name)
                
                if table is None:
                    continue

                for key in keys:
                    try:
                        topic = f"/{table_name}/{key}"
                        val = read_any(table, key)

                        # Só envia se mudou
                        if last.get(topic) != val:
                            last[topic] = val
                            msg = json.dumps({"topic": topic, "value": val})

                            # Envia para todos os clientes conectados
                            dead = []
                            for ws in clients.copy():  # Cópia para evitar race condition
                                try:
                                    await ws.send(msg)
                                except websockets.exceptions.ConnectionClosed:
                                    dead.append(ws)
                                except Exception as e:
                                    print(f"⚠️ Erro ao enviar {topic}: {type(e).__name__}: {e}")
                                    dead.append(ws)

                            # Remove clientes desconectados
                            for ws in dead:
                                clients.discard(ws)
                                print(f"🔌 Cliente removido (total: {len(clients)})")

                            print(f"📡 {topic} = {val}")
                            error_count = 0
                            
                    except Exception as e:
                        print(f"❌ Erro ao ler {table_name}/{key}: {type(e).__name__}: {e}")

            await asyncio.sleep(POLL_INTERVAL)
            
        except Exception as e:
            error_count += 1
            print(f"❌ Erro crítico em poll_and_broadcast: {type(e).__name__}: {e}")
            
            if error_count >= max_errors:
                print("❌ Muitos erros! Reiniciando poll...")
                error_count = 0
            
            await asyncio.sleep(1.0)


# ================= WEBSOCKET =================

async def handle_ws(ws, path):
    """
    Manipula conexões WebSocket
    - Envia snapshot inicial
    - Recebe comandos do StreamDeck
    - Executa ações no NetworkTables
    """
    remote_addr = ws.remote_address
    print(f"✅ WS conectado: {remote_addr} (total: {len(clients) + 1})")
    clients.add(ws)

    try:
        # ===== SNAPSHOT INICIAL =====
        print(f"📸 Enviando snapshot para {remote_addr}")
        for table_name, keys in TABLES_AND_KEYS.items():
            table = get_table(table_name)
            
            if table is None:
                continue
                
            for key in keys:
                try:
                    val = read_any(table, key)
                    await ws.send(json.dumps({
                        "topic": f"/{table_name}/{key}",
                        "value": val
                    }))
                except Exception as e:
                    print(f"⚠️ Erro ao enviar snapshot {table_name}/{key}: {e}")

        # ===== LOOP DE RECEBIMENTO =====
        async for message in ws:
            try:
                obj = json.loads(message)
                print(f"📥 Recebido: {obj}")

                # ===== STREAMDECK PRESS (BOOLEAN PULSE) =====
                if obj.get("action") == "press":
                    table_name = obj.get("table")
                    key_name = obj.get("key")

                    if not table_name or not key_name:
                        print(f"❌ Press inválido: {obj}")
                        continue

                    if not is_valid_table_key(table_name, key_name):
                        print(f"❌ Tabela/chave não autorizada: {table_name}/{key_name}")
                        continue

                    table = get_table(table_name)
                    
                    if table is None:
                        print(f"❌ Não conseguiu obter tabela: {table_name}")
                        continue

                    # Cria task assíncrona para o pulse (melhor que call_later)
                    asyncio.create_task(pulse_button(table, key_name, PULSE_TIME))
                    continue

                # ===== PUT GENÉRICO =====
                if obj.get("action") == "put":
                    table_name = obj.get("table")
                    key = obj.get("key")
                    value = obj.get("value")

                    if not table_name or not key or value is None:
                        print(f"❌ PUT inválido: {obj}")
                        continue

                    if not is_valid_table_key(table_name, key):
                        print(f"❌ Tabela/chave não autorizada: {table_name}/{key}")
                        continue

                    table = get_table(table_name)
                    
                    if table is None:
                        print(f"❌ Não conseguiu obter tabela: {table_name}")
                        continue

                    try:
                        if isinstance(value, list):
                            table.putNumberArray(key, value)
                        elif isinstance(value, bool):
                            table.putBoolean(key, value)
                        elif isinstance(value, (int, float)):
                            table.putNumber(key, value)
                        else:
                            table.putString(key, str(value))

                        print(f"✅ PUT: {table_name}/{key} = {value}")
                        
                    except Exception as e:
                        print(f"❌ Erro ao fazer PUT: {type(e).__name__}: {e}")

            except json.JSONDecodeError:
                print("❌ Erro: JSON inválido recebido")
            except Exception as e:
                print(f"❌ Erro ao processar mensagem: {type(e).__name__}: {e}")

    except websockets.exceptions.ConnectionClosed:
        print(f"🔌 WS fechado normalmente: {remote_addr}")
    except Exception as e:
        print(f"❌ WS erro: {type(e).__name__}: {e}")
    finally:
        clients.discard(ws)
        print(f"🔌 WS desconectado: {remote_addr} (total: {len(clients)})")


# ================= MAIN =================

async def main_async(roborio, port):
    """Inicializa NetworkTables, WebSocket e tasks assíncronas"""
    
    # Conecta ao RoboRIO
    connect_nt(roborio)

    # Inicia servidor WebSocket
    print(f"🚀 Iniciando WebSocket em ws://0.0.0.0:{port}{WS_PATH}")
    server = await websockets.serve(handle_ws, WS_HOST, port, ping_interval=20, ping_timeout=10)
    print(f"✅ WebSocket pronto!")

    # Cria tasks
    poll_task = asyncio.create_task(poll_and_broadcast())
    monitor_task = asyncio.create_task(monitor_nt_connection(roborio))

    print("=" * 50)
    print("✅ Sistema StreamDeck PRONTO")
    print(f"   RoboRIO: {roborio}")
    print(f"   WebSocket: ws://localhost:{port}")
    print("=" * 50)

    try:
        await server.wait_closed()
    except KeyboardInterrupt:
        print("\n🛑 Encerrando...")
    finally:
        poll_task.cancel()
        monitor_task.cancel()


def main():
    """Entry point"""
    parser = argparse.ArgumentParser(description="StreamDeck NetworkTables Bridge")
    parser.add_argument("--roborio", default=DEFAULT_ROBORIO, help=f"IP do RoboRIO (padrão: {DEFAULT_ROBORIO})")
    parser.add_argument("--port", type=int, default=DEFAULT_WS_PORT, help=f"Porta WebSocket (padrão: {DEFAULT_WS_PORT})")
    args = parser.parse_args()

    try:
        asyncio.run(main_async(args.roborio, args.port))
    except KeyboardInterrupt:
        print("\n🛑 Encerrado pelo usuário")


if __name__ == "__main__":
    main()