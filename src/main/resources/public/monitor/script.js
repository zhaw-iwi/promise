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
let logSettings = {
  level: "INFO",
  loggers: new Set(["ch.zhaw.statefulconversation.model.State"]),
  maxChars: 600,
  showTimestamps: false,
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
  loadStorage();
  refreshCurrentState();
  startPolling();
});

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

async function loadStorage() {
  const response = await fetch(`/${session.agentId}/storage`);
  if (!response.ok) {
    appendLog("app", "Unable to load storage.");
    return;
  }
  const data = await response.json();
  const snapshot = serializeStorage(data);
  if (snapshot === session.storageSnapshot) {
    return;
  }
  session.storageSnapshot = snapshot;
  session.storage = data;
  renderStorageList();
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
    await loadStorage();
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
