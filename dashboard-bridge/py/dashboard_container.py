import threading
import time

import limelight.AI_Data # seu arquivo AI_Data.py
import bridge.nt3_ws            # seu arquivo nt3_ws.py

def run_horizontal():
    # chama o loop do AI_Data (precisa existir main_loop lá)
    limelight.AI_Data.main_loop()

def run_ws():
    # roda o websocket do nt3_ws no mesmo processo
    # equivalente a: python nt3_ws.py --roborio 10.91.63.2 --port 5810
    import asyncio
    asyncio.run(bridge.nt3_ws.main_async("10.91.63.2", 5810))

if __name__ == "__main__":
    t1 = threading.Thread(target=run_horizontal, daemon=True)
    t2 = threading.Thread(target=run_ws, daemon=True)

    t1.start()
    t2.start()

    # mantém vivo
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        pass
