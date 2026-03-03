let session = {
  agentId: null,
  currentState: null,
  innerState: null,
  innerChain: [],
  states: [],
  storage: [],
  openStorageKeys: new Set(),
  storageSnapshot: "",
};

let logSource = null;
let storageSource = null;
let stateSource = null;
let logReconnectTimer = null;
let storageReconnectTimer = null;
let stateReconnectTimer = null;
const reconnectBaseDelayMs = 1000;
const reconnectMaxDelayMs = 30000;
const reconnectJitterMs = 250;
let logReconnectAttempts = 0;
let stateReconnectAttempts = 0;
let storageReconnectAttempts = 0;
let logSettings = {
  level: "INFO",
  loggers: new Set(["ch.zhaw.statefulconversation.model.State"]),
  maxChars: 600,
  showTimestamps: false,
};
let logBuffer = [];

window.addEventListener("load", () => {
  session.agentId = getAgentId();
  if (!session.agentId) {
    appendLog("app", "Missing agent id in URL. Use ?{UUID} or ?agentId=UUID.");
    disableUi();
    return;
  }
  wireUi();
  connectLogs();
  loadAgentInfo();
  loadStates();
  initStateStream();
  initStorageStream();
});

window.addEventListener("beforeunload", closeStreams);
window.addEventListener("pagehide", closeStreams);

function wireUi() {
  document.getElementById("show_agent_info").addEventListener("click", showAgentInfo);
  const loggerInput = document.getElementById("log_logger_filter");
  const levelSelect = document.getElementById("log_level_filter");
  const maxCharsInput = document.getElementById("log_max_chars");
  const timestampToggle = document.getElementById("log_show_timestamps");
  const copyButton = document.getElementById("log_copy");
  const clearButton = document.getElementById("log_clear");

  loggerInput.addEventListener("change", () => {
    const selected = Array.from(loggerInput.selectedOptions)
      .map((option) => option.value.trim())
      .filter((value) => value.length > 0);
    logSettings.loggers = new Set(selected);
    renderLogBuffer();
  });
  levelSelect.addEventListener("change", () => {
    logSettings.level = levelSelect.value;
    renderLogBuffer();
  });
  maxCharsInput.addEventListener("change", () => {
    const parsed = Number.parseInt(maxCharsInput.value, 10);
    if (!Number.isNaN(parsed) && parsed > 0) {
      logSettings.maxChars = parsed;
      renderLogBuffer();
    }
  });
  timestampToggle.addEventListener("change", () => {
    logSettings.showTimestamps = timestampToggle.checked;
    renderLogBuffer();
  });
  clearButton.addEventListener("click", () => {
    logBuffer = [];
    document.getElementById("log_output").textContent = "";
  });
  copyButton.addEventListener("click", () => {
    const output = document.getElementById("log_output").textContent || "";
    copyToClipboard(output);
  });
}

function disableUi() {
  document.getElementById("show_agent_info").disabled = true;
}

function setActiveStatus(isActive) {
  const status = document.getElementById("active_status");
  if (isActive === true) {
    status.textContent = "Active";
    status.className = "status-pill is-active";
  } else if (isActive === false) {
    status.textContent = "Inactive";
    status.className = "status-pill is-inactive";
  } else {
    status.textContent = "Unknown";
    status.className = "status-pill is-unknown";
  }
}

function updateCurrentState(stateName, innerName, innerChain) {
  session.currentState = stateName;
  session.innerState = innerName;
  session.innerChain = Array.isArray(innerChain) ? innerChain : [];
  const innermost = session.innerChain.length
    ? session.innerChain[session.innerChain.length - 1]
    : innerName || stateName || "Unknown";
  document.getElementById("current_state").textContent = innermost;
  renderStateList();
}

function renderStateList() {
  const list = document.getElementById("state_list");
  list.innerHTML = "";
  session.states.forEach((stateName) => {
    const item = document.createElement("li");
    item.className = "list-group-item d-flex justify-content-between align-items-center";
    item.textContent = stateName;
    if (stateName === session.currentState || session.innerChain.includes(stateName)) {
      const badge = document.createElement("span");
      badge.className = "badge text-bg-light";
      badge.textContent = "current";
      item.appendChild(badge);
    }
    list.appendChild(item);
  });
}

function renderStorageList() {
  session.openStorageKeys = getOpenStorageKeys();
  const list = document.getElementById("storage_list");
  list.innerHTML = "";
  if (!session.storage.length) {
    const item = document.createElement("div");
    item.className = "list-group-item";
    item.textContent = "No storage entries.";
    list.appendChild(item);
    return;
  }
  session.storage.forEach((entry, index) => {
    const keyValue = entry.key || "unknown";
    const safeKey = toSafeId(keyValue);
    const item = document.createElement("div");
    item.className = "list-group-item p-0";
    const headerId = `storage_header_${safeKey}_${index}`;
    const collapseId = `storage_collapse_${safeKey}_${index}`;

    const header = document.createElement("div");
    header.className = "d-flex align-items-center justify-content-between px-3 py-2 gap-2";

    const button = document.createElement("button");
    button.className =
      "btn btn-link text-start flex-grow-1 fw-semibold text-decoration-none text-body p-0";
    button.type = "button";
    button.setAttribute("data-bs-toggle", "collapse");
    button.setAttribute("data-bs-target", `#${collapseId}`);
    button.setAttribute("aria-expanded", "false");
    button.setAttribute("aria-controls", collapseId);
    button.id = headerId;
    button.textContent = keyValue;
    button.dataset.storageKey = keyValue;

    const copyButton = document.createElement("button");
    copyButton.className = "btn btn-outline-ink btn-sm";
    copyButton.type = "button";
    copyButton.title = "Copy value";
    copyButton.innerHTML = '<i class="bi bi-clipboard"></i>';
    copyButton.addEventListener("click", (event) => {
      event.preventDefault();
      event.stopPropagation();
      copyToClipboard(formatStorageValue(entry.value));
    });

    header.appendChild(button);
    header.appendChild(copyButton);

    const collapse = document.createElement("div");
    collapse.className = "collapse";
    collapse.id = collapseId;
    collapse.setAttribute("aria-labelledby", headerId);
    collapse.setAttribute("data-bs-parent", "#storage_list");
    if (session.openStorageKeys.has(keyValue)) {
      collapse.classList.add("show");
      button.setAttribute("aria-expanded", "true");
    }

    const body = document.createElement("div");
    body.className = "px-3 pb-3";

    const value = document.createElement("pre");
    value.className = "mono small mb-0";
    value.textContent = formatStorageValue(entry.value);

    body.appendChild(value);
    collapse.appendChild(body);
    item.appendChild(header);
    item.appendChild(collapse);
    list.appendChild(item);
  });
}

function getOpenStorageKeys() {
  const openKeys = new Set();
  document.querySelectorAll("#storage_list .collapse.show").forEach((element) => {
    const headerId = element.getAttribute("aria-labelledby");
    if (!headerId) {
      return;
    }
    const button = document.getElementById(headerId);
    if (button && button.dataset.storageKey) {
      openKeys.add(button.dataset.storageKey);
    }
  });
  return openKeys;
}

function toSafeId(value) {
  return encodeURIComponent(value)
    .replace(/%/g, "_")
    .replace(/[^a-zA-Z0-9_-]/g, "_");
}

function formatStorageValue(rawValue) {
  if (rawValue === null || rawValue === undefined) {
    return "";
  }
  if (typeof rawValue !== "string") {
    try {
      return JSON.stringify(rawValue, null, 2);
    } catch (error) {
      return String(rawValue);
    }
  }
  const trimmed = rawValue.trim();
  if (!trimmed) {
    return "";
  }
  try {
    const parsed = JSON.parse(trimmed);
    return JSON.stringify(parsed, null, 2);
  } catch (error) {
    return rawValue;
  }
}

function serializeStorage(entries) {
  try {
    return JSON.stringify(entries ?? []);
  } catch (error) {
    return String(entries);
  }
}

function copyToClipboard(value) {
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(value);
    return;
  }
  const fallback = document.createElement("textarea");
  fallback.value = value;
  fallback.style.position = "fixed";
  fallback.style.opacity = "0";
  document.body.appendChild(fallback);
  fallback.select();
  document.execCommand("copy");
  document.body.removeChild(fallback);
}

function connectLogs() {
  if (logSource) {
    logSource.close();
  }
  if (logReconnectTimer) {
    clearTimeout(logReconnectTimer);
    logReconnectTimer = null;
  }
  logSource = new EventSource("/logs/stream");
  logSource.onopen = () => {
    logReconnectAttempts = 0;
  };
  logSource.addEventListener("log", (event) => {
    const data = JSON.parse(event.data);
    addLogEntry({
      timestamp: new Date(data.timestamp || Date.now()).toLocaleTimeString(),
      source: "",
      message: data.message || "",
      level: data.level,
      logger: data.logger,
      isLogEvent: true,
    });
  });
  logSource.onerror = () => {
    appendLog("app", "Log stream disconnected.");
    if (logSource) {
      logSource.close();
      logSource = null;
    }
    scheduleLogReconnect();
  };
}

function closeStreams() {
  if (logReconnectTimer) {
    clearTimeout(logReconnectTimer);
    logReconnectTimer = null;
  }
  if (stateReconnectTimer) {
    clearTimeout(stateReconnectTimer);
    stateReconnectTimer = null;
  }
  if (storageReconnectTimer) {
    clearTimeout(storageReconnectTimer);
    storageReconnectTimer = null;
  }
  logReconnectAttempts = 0;
  stateReconnectAttempts = 0;
  storageReconnectAttempts = 0;
  if (logSource) {
    logSource.close();
    logSource = null;
  }
  if (stateSource) {
    stateSource.close();
    stateSource = null;
  }
  if (storageSource) {
    storageSource.close();
    storageSource = null;
  }
}

function initStorageStream() {
  if (!window.EventSource) {
    appendLog("app", "Storage stream unavailable. Streaming required.");
    return;
  }
  connectStorageStream();
}

function initStateStream() {
  if (!window.EventSource) {
    appendLog("app", "State stream unavailable. Streaming required.");
    return;
  }
  connectStateStream();
}

function connectStateStream() {
  if (stateSource) {
    stateSource.close();
  }
  if (stateReconnectTimer) {
    clearTimeout(stateReconnectTimer);
    stateReconnectTimer = null;
  }
  stateSource = new EventSource(`/${session.agentId}/state/stream`);
  stateSource.onopen = () => {
    stateReconnectAttempts = 0;
  };
  stateSource.addEventListener("state", (event) => {
    try {
      const data = JSON.parse(event.data);
      applyStateUpdate(data);
    } catch (error) {
      appendLog("app", "Unable to parse state stream update.");
    }
  });
  stateSource.onerror = () => {
    appendLog("app", "State stream disconnected.");
    if (stateSource) {
      stateSource.close();
      stateSource = null;
    }
    scheduleStateReconnect();
  };
}

function connectStorageStream() {
  if (storageSource) {
    storageSource.close();
  }
  if (storageReconnectTimer) {
    clearTimeout(storageReconnectTimer);
    storageReconnectTimer = null;
  }
  storageSource = new EventSource(`/${session.agentId}/storage/stream`);
  storageSource.onopen = () => {
    storageReconnectAttempts = 0;
  };
  storageSource.addEventListener("storage", (event) => {
    try {
      const data = JSON.parse(event.data);
      applyStorageUpdate(data);
    } catch (error) {
      appendLog("app", "Unable to parse storage stream update.");
    }
  });
  storageSource.onerror = () => {
    appendLog("app", "Storage stream disconnected.");
    if (storageSource) {
      storageSource.close();
      storageSource = null;
    }
    scheduleStorageReconnect();
  };
}

function scheduleLogReconnect() {
  if (logReconnectTimer) {
    return;
  }
  const delay = nextReconnectDelayMs(logReconnectAttempts);
  logReconnectAttempts += 1;
  logReconnectTimer = setTimeout(() => {
    logReconnectTimer = null;
    connectLogs();
  }, delay);
}

function scheduleStateReconnect() {
  if (stateReconnectTimer || !session.agentId) {
    return;
  }
  const delay = nextReconnectDelayMs(stateReconnectAttempts);
  stateReconnectAttempts += 1;
  stateReconnectTimer = setTimeout(() => {
    stateReconnectTimer = null;
    connectStateStream();
  }, delay);
}

function scheduleStorageReconnect() {
  if (storageReconnectTimer || !session.agentId) {
    return;
  }
  const delay = nextReconnectDelayMs(storageReconnectAttempts);
  storageReconnectAttempts += 1;
  storageReconnectTimer = setTimeout(() => {
    storageReconnectTimer = null;
    connectStorageStream();
  }, delay);
}

function nextReconnectDelayMs(attempt) {
  const exponential = reconnectBaseDelayMs * Math.pow(2, Math.max(0, attempt));
  const bounded = Math.min(reconnectMaxDelayMs, exponential);
  const jitter = Math.floor(Math.random() * reconnectJitterMs);
  return bounded + jitter;
}

async function loadAgentInfo() {
  const response = await fetch(`/${session.agentId}/info`);
  if (!response.ok) {
    appendLog("app", "Unable to load agent info.");
    return;
  }
  const data = await response.json();
  document.getElementById("agent_name").textContent = data.name;
  setActiveStatus(data.active);
}

async function showAgentInfo() {
  const response = await fetch(`/${session.agentId}/info`);
  if (!response.ok) {
    appendLog("app", "Unable to load agent info.");
    return;
  }
  const data = await response.json();
  alert(`Name\n${data.name}\n\nDescription\n${data.description}`);
}

async function loadStates() {
  const response = await fetch(`/${session.agentId}/states`);
  if (!response.ok) {
    appendLog("app", "Unable to load states.");
    return;
  }
  session.states = await response.json();
  renderStateList();
}

function applyStorageUpdate(data) {
  const entries = Array.isArray(data) ? data : [];
  const snapshot = serializeStorage(entries);
  if (snapshot === session.storageSnapshot) {
    return;
  }
  session.storageSnapshot = snapshot;
  session.storage = entries;
  renderStorageList();
}

function applyStateUpdate(data) {
  if (!data || !data.state) {
    return;
  }
  const state = data.state;
  updateCurrentState(state.name, state.innerName, state.innerNames);
  setActiveStatus(data.active);
}

function appendLog(source, message) {
  addLogEntry({
    timestamp: new Date().toLocaleTimeString(),
    source,
    message,
    isLogEvent: false,
  });
}

function addLogEntry(entry) {
  logBuffer.push(entry);
  renderLogBuffer();
}

function renderLogBuffer() {
  const output = document.getElementById("log_output");
  output.textContent = "";
  logBuffer.forEach((entry) => {
    if (entry.isLogEvent && !shouldIncludeLog(entry)) {
      return;
    }
    const message = entry.isLogEvent
      ? truncateLogMessage(entry.message || "", logSettings.maxChars)
      : entry.message || "";
    const sourcePrefix = entry.source ? `${entry.source}: ` : "";
    const timestampPrefix = logSettings.showTimestamps ? `[${entry.timestamp}] ` : "";
    output.textContent += `${timestampPrefix}${sourcePrefix}${message}\n`;
  });
  output.scrollTop = output.scrollHeight;
}

function shouldIncludeLog(entry) {
  const level = (entry.level || "").toUpperCase();
  const logger = entry.logger || "";
  if (logSettings.level && level !== logSettings.level.toUpperCase()) {
    return false;
  }
  if (logSettings.loggers.size > 0) {
    const matches = Array.from(logSettings.loggers).some((filter) => logger.includes(filter));
    if (!matches) {
      return false;
    }
  }
  return true;
}

function truncateLogMessage(message, maxChars) {
  if (!maxChars || message.length <= maxChars) {
    return message;
  }
  return message.slice(0, maxChars) + "...";
}

function getAgentId() {
  const search = window.location.search;
  if (!search || search.length < 2) {
    return null;
  }
  if (search.includes("=")) {
    const params = new URLSearchParams(search);
    if (params.has("agentId")) {
      return params.get("agentId");
    }
    if (params.has("agent")) {
      return params.get("agent");
    }
  }
  return search.substring(1);
}
