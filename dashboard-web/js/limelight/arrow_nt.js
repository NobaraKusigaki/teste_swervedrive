// LIMELIGHT ARROW CONTROLLER (WebSocket bridge, NT3 -> Browser)

const UPDATE_RATE_MS = 50;

// thresholds de seta
const TX_DEADBAND = 2;
const TX_MEDIUM   = 5;
const TX_STRONG   = 8;

// WebSocket
const WS_URL = "ws://127.0.0.1:5810/nt/dashboard";

// NT topics
const KEY_HAS_TARGET   = "/limelight-back/has_target";
const KEY_TX           = "/limelight-back/piece_tx";
const KEY_BBOX         = "/limelight-back/bbox";
const KEY_ALIGN_LIME2  = "/Modes/AlignLime2"; // 0=OFF, 1=ON, 2=AUTO

function setupLimelightArrowWS(config) {
  const { arrowId, parentSelector, imgSelector } = config;

  const arrow  = document.getElementById(arrowId);
  const parent = document.querySelector(parentSelector);
  const img    = parent ? parent.querySelector(imgSelector) : null;

  if (!arrow || !parent || !img) {
    console.warn("Limelight não inicializada:", config);
    return;
  }

  // -------------------------
  // estilos (fade)
  // -------------------------
  arrow.style.transition = "opacity 150ms ease";
  arrow.style.opacity = "0";

  // BBOX DIV
  const bboxDiv = document.createElement("div");
  bboxDiv.className = "bbox";
  bboxDiv.style.position = "absolute";
  bboxDiv.style.pointerEvents = "none";
  bboxDiv.style.opacity = "0";
  bboxDiv.style.transition = "opacity 150ms ease";
  parent.appendChild(bboxDiv);

  // -------------------------
  // helpers visuais
  // -------------------------
  function hideArrow() {
    arrow.classList.add("hidden");
    arrow.style.opacity = "0";
    arrow.textContent = "";
  }

  function showArrow() {
    arrow.classList.remove("hidden");
    arrow.style.opacity = "1";
  }

  function hideBBox() {
    bboxDiv.style.opacity = "0";
  }

  function showBBox() {
    bboxDiv.style.opacity = "1";
  }

  function atualizarSeta(tx) {
    if (tx === null || tx === undefined || Math.abs(tx) <= TX_DEADBAND) {
      hideArrow();
      return;
    }

    arrow.classList.remove("arrow-left", "arrow-right");
    arrow.textContent = "";

    let level = 1;
    if (Math.abs(tx) >= TX_STRONG) level = 3;
    else if (Math.abs(tx) >= TX_MEDIUM) level = 2;

    if (tx > 0) {
      arrow.textContent = ">".repeat(level);
      arrow.classList.add("arrow-left");
    } else {
      arrow.textContent = "<".repeat(level);
      arrow.classList.add("arrow-right");
    }

    showArrow();
  }

  function atualizarBBox(bbox) {
    if (
      !img.complete ||
      !bbox ||
      bbox.length !== 4 ||
      !img.naturalWidth ||
      !img.naturalHeight
    ) {
      hideBBox();
      return;
    }

    const [x1, y1, x2, y2] = bbox;
    const rect = img.getBoundingClientRect();

    const scaleX = rect.width / img.naturalWidth;
    const scaleY = rect.height / img.naturalHeight;

    bboxDiv.style.left   = (x1 * scaleX) + "px";
    bboxDiv.style.top    = (y1 * scaleY) + "px";
    bboxDiv.style.width  = ((x2 - x1) * scaleX) + "px";
    bboxDiv.style.height = ((y2 - y1) * scaleY) + "px";

    showBBox();
  }

  // -------------------------
  // estado local
  // -------------------------
  let hasTarget   = false;
  let tx          = null;
  let bbox        = null;
  let alignEnabled = false;

  function render() {
    // regra principal:
    // só mostra seta/bbox se alinhamento ON/AUTO E tem alvo
    if (!hasTarget || !alignEnabled) {
      hideArrow();
      hideBBox();
      return;
    }

    atualizarSeta(tx);
    atualizarBBox(bbox);
  }

  // -------------------------
  // WebSocket
  // -------------------------
  let ws = null;
  let lastMsgTs = 0;

  function connect() {
    ws = new WebSocket(WS_URL);

    ws.onopen = () => {
      hasTarget = false;
      tx = null;
      bbox = null;
      alignEnabled = false;
      render();
    };

    ws.onmessage = (ev) => {
      lastMsgTs = Date.now();

      let obj;
      try { obj = JSON.parse(ev.data); } catch { return; }

      const topic = obj.topic;
      const value = obj.value;

      if (topic === KEY_ALIGN_LIME2) {
        alignEnabled = Number(value) !== 0; // ON ou AUTO
        render();
        return;
      }

      if (topic === KEY_HAS_TARGET) {
        hasTarget = Boolean(value);
        render();
        return;
      }

      if (topic === KEY_TX) {
        tx = (value === null || value === undefined) ? null : Number(value);
        render();
        return;
      }

      if (topic === KEY_BBOX) {
        bbox = (Array.isArray(value) && value.length === 4)
          ? value.map(Number)
          : null;
        render();
        return;
      }
    };

    ws.onclose = () => {
      hasTarget = false;
      tx = null;
      bbox = null;
      alignEnabled = false;
      render();
      setTimeout(connect, 500);
    };

    ws.onerror = () => {
      try { ws.close(); } catch {}
    };
  }

  // watchdog: se parar de chegar dado, desliga overlay
  setInterval(() => {
    if (Date.now() - lastMsgTs > 2000) {
      hasTarget = false;
      tx = null;
      bbox = null;
      render();
    }
  }, UPDATE_RATE_MS);

  render();
  connect();
}

// ==========================
// INIT (Lime 2+)
// ==========================
setupLimelightArrowWS({
  arrowId: "arrow-lime2",
  parentSelector: "#lime2 .arrow-parent",
  imgSelector: "img",
});
