let session = {
  agentId: null,
  currentState: null,
  innerState: null,
  innerChain: [],
  states: [],
};

let logSource = null;
let logSettings = {
  level: "INFO",
  loggers: new Set(["ch.zhaw.statefulconversation.model.State"]),
  maxChars: 600,
};
let logBuffer = [];
let statePoller = null;

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
  refreshCurrentState();
  startPolling();
});

function wireUi() {
  document.getElementById("show_agent_info").addEventListener("click", showAgentInfo);
  const loggerInput = document.getElementById("log_logger_filter");
  const levelSelect = document.getElementById("log_level_filter");
  const maxCharsInput = document.getElementById("log_max_chars");
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
  clearButton.addEventListener("click", () => {
    logBuffer = [];
    document.getElementById("log_output").textContent = "";
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

function connectLogs() {
  if (logSource) {
    logSource.close();
  }
  logSource = new EventSource("/logs/stream");
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
  };
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

async function refreshCurrentState() {
  const response = await fetch(`/${session.agentId}/state`);
  if (!response.ok) {
    appendLog("app", "Unable to load current state.");
    return;
  }
  const data = await response.json();
  updateCurrentState(data.name, data.innerName, data.innerNames);
}

function startPolling() {
  if (statePoller) {
    clearInterval(statePoller);
  }
  statePoller = setInterval(async () => {
    await refreshCurrentState();
    await refreshActiveStatus();
  }, 2000);
}

async function refreshActiveStatus() {
  const response = await fetch(`/${session.agentId}/info`);
  if (!response.ok) {
    return;
  }
  const data = await response.json();
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
    output.textContent += `[${entry.timestamp}] ${sourcePrefix}${message}\n`;
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
