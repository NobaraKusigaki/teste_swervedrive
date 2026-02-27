const WebSocket = require("ws");

const COMMAND = process.argv[2];

if (!COMMAND) {
  console.log("Uso:");
  console.log(" node send_sd_command.js CW");
  console.log(" node send_sd_command.js CCW");
  console.log(" node send_sd_command.js STOP");
  process.exit(1);
}

const ws = new WebSocket("ws://127.0.0.1:5810");

ws.on("open", () => {
  ws.send(JSON.stringify({
    action: "put",
    value: COMMAND
  }));

  console.log("📡 Enviado:", COMMAND);
  setTimeout(() => ws.close(), 200);
});
