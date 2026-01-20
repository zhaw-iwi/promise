let session = {
  agentId: null,
  isListening: false,
};

let peerConnection = null;
let dataChannel = null;
let micStream = null;
let assistantTranscriptBuffer = "";
let suppressAssistantAppend = false;
let manualResponseConfigSent = false;
let lastSystemPrompt = "";
let sessionSettings = {
  voice: "",
  temperature: 0.7,
  turnDetection: "server_vad",
};
let pushToTalkActive = false;

window.addEventListener("load", () => {
  session.agentId = getAgentId();
  if (!session.agentId) {
    appendLog("app", "Missing agent id in URL. Use ?{UUID} or ?agentId=UUID.");
    disableToggle();
    return;
  }
  wireUi();
  loadAgentInfo();
});

function wireUi() {
  document.getElementById("toggle_listen").addEventListener("click", toggleListening);
  document.getElementById("reset_agent").addEventListener("click", resetAgent);
  document.getElementById("show_agent_info").addEventListener("click", showAgentInfo);
  const voiceSelect = document.getElementById("voice_select");
  const temperatureInput = document.getElementById("temperature_input");
  const turnDetectionSelect = document.getElementById("turn_detection_select");
  const pushToTalkButton = document.getElementById("push_to_talk");
  if (voiceSelect) {
    voiceSelect.addEventListener("change", () => {
      sessionSettings.voice = voiceSelect.value;
      applySessionSettings();
    });
  }
  if (temperatureInput) {
    temperatureInput.addEventListener("input", () => {
      sessionSettings.temperature = Number.parseFloat(temperatureInput.value);
      applySessionSettings();
    });
  }
  if (turnDetectionSelect) {
    turnDetectionSelect.addEventListener("change", () => {
      sessionSettings.turnDetection = turnDetectionSelect.value;
      updatePushToTalkUi();
      applySessionSettings();
    });
  }
  if (pushToTalkButton) {
    pushToTalkButton.addEventListener("mousedown", startPushToTalk);
    pushToTalkButton.addEventListener("touchstart", startPushToTalk, { passive: false });
    pushToTalkButton.addEventListener("mouseup", stopPushToTalk);
    pushToTalkButton.addEventListener("mouseleave", stopPushToTalk);
    pushToTalkButton.addEventListener("touchend", stopPushToTalk);
  }
  updatePushToTalkUi();
}

function disableToggle() {
  const button = document.getElementById("toggle_listen");
  button.disabled = true;
}

function setListeningState(isListening) {
  session.isListening = isListening;
  const button = document.getElementById("toggle_listen");
  const status = document.getElementById("listen_status");
  const gif = document.getElementById("realtime_gif");
  if (isListening) {
    button.innerHTML = '<i class="bi bi-mic-mute-fill me-2"></i>Stop';
    status.textContent = "Listening";
    status.className = "status-pill is-listening";
    button.classList.add("is-listening");
    updatePushToTalkUi();
    if (gif) {
      gif.classList.remove("is-hidden");
    }
  } else {
    button.innerHTML = '<i class="bi bi-mic-fill me-2"></i>Start';
    status.textContent = "Idle";
    status.className = "status-pill is-idle";
    button.classList.remove("is-listening");
    updatePushToTalkUi();
    if (gif) {
      gif.classList.add("is-hidden");
    }
  }
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

function applyPromptBundle(promptBundle, shouldRespond) {
  sendSessionUpdate(buildSystemPrompt(promptBundle));
  if (!shouldRespond) {
    return;
  }
  const responseInstruction = buildResponseInstruction(promptBundle);
  sendResponseCreate(responseInstruction);
}

function buildSystemPrompt(promptBundle) {
  if (!promptBundle) {
    return "";
  }
  const basePrompt = promptBundle.systemPrompt || "";
  const conversationBlock = buildConversationContext(promptBundle.conversation || []);
  if (!conversationBlock) {
    return basePrompt;
  }
  if (!basePrompt) {
    return conversationBlock;
  }
  return `${basePrompt}\n\n${conversationBlock}`;
}

function buildConversationContext(conversation) {
  if (!Array.isArray(conversation) || conversation.length === 0) {
    return "";
  }
  const lines = conversation
    .map((utterance) => {
      if (!utterance) {
        return null;
      }
      const role = utterance.role || "unknown";
      const content = utterance.content || "";
      return `${role}: ${content}`;
    })
    .filter((line) => line && line.trim().length > 0);
  if (lines.length === 0) {
    return "";
  }
  return `Conversation so far:\n${lines.join("\n")}`;
}

function buildResponseInstruction(promptBundle) {
  if (promptBundle && Array.isArray(promptBundle.conversation) && promptBundle.conversation.length === 0) {
    return "Begin the conversation now.";
  }
  return "Respond to the user's latest message.";
}

async function toggleListening() {
  if (!session.isListening) {
    await startListening();
  } else {
    await stopListening();
  }
}

async function startListening() {
  appendLog("app", "Starting realtime session...");
  setListeningState(true);
  try {
    const [promptBundle, conversation] = await Promise.all([
      fetchPromptBundle(),
      fetchConversation(),
    ]);
    setActiveStatus(promptBundle.active);
    const sessionInfo = await createRealtimeSession();
    await setupRealtimeConnection(sessionInfo);
    await waitForDataChannelOpen();
    configureManualResponses();
    applySessionSettings();
    updatePushToTalkUi();
    const lastAssistantFromHistory = getLastAssistantUtterance(conversation || []);
    if (lastAssistantFromHistory) {
      sendSessionUpdate(buildSystemPrompt(promptBundle));
      speakStoredAssistantUtterance(lastAssistantFromHistory);
    } else {
      applyPromptBundle(promptBundle, true);
    }
  } catch (error) {
    appendLog("app", "Failed to start: " + error.message);
    await stopListening();
  }
}

async function stopListening() {
  appendLog("app", "Stopping realtime session...");
  setListeningState(false);
  if (dataChannel) {
    dataChannel.close();
    dataChannel = null;
  }
  if (peerConnection) {
    peerConnection.close();
    peerConnection = null;
  }
  if (micStream) {
    micStream.getTracks().forEach((track) => track.stop());
    micStream = null;
  }
  manualResponseConfigSent = false;
  pushToTalkActive = false;
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

async function fetchConversation() {
  const response = await fetch(`/${session.agentId}/conversation`);
  if (!response.ok) {
    appendLog("app", "Unable to load conversation history.");
    return [];
  }
  return await response.json();
}

async function fetchPromptBundle() {
  const response = await fetch(`/${session.agentId}/prompt`);
  if (!response.ok) {
    throw new Error("PROMISE prompt fetch failed.");
  }
  const data = await response.json();
  appendLog("promise", "Prompt bundle received.");
  return data;
}

async function createRealtimeSession() {
  const response = await fetch("/realtime/session", {
    method: "POST",
  });
  if (!response.ok) {
    throw new Error("Realtime session creation failed.");
  }
  return await response.json();
}

async function setupRealtimeConnection(sessionInfo) {
  peerConnection = new RTCPeerConnection();
  peerConnection.ontrack = (event) => {
    const audio = document.getElementById("assistant_audio");
    audio.srcObject = event.streams[0];
  };

  dataChannel = peerConnection.createDataChannel("oai-events");
  dataChannel.addEventListener("message", handleRealtimeEvent);

  micStream = await navigator.mediaDevices.getUserMedia({ audio: true });
  micStream.getTracks().forEach((track) => peerConnection.addTrack(track, micStream));

  const offer = await peerConnection.createOffer();
  await peerConnection.setLocalDescription(offer);

  const answerResponse = await fetch(
    `${sessionInfo.realtimeUrl}?model=${encodeURIComponent(sessionInfo.model)}`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${sessionInfo.clientSecret}`,
        "Content-Type": "application/sdp",
      },
      body: offer.sdp,
    }
  );

  if (!answerResponse.ok) {
    throw new Error("Realtime SDP exchange failed.");
  }

  const answer = await answerResponse.text();
  await peerConnection.setRemoteDescription({ type: "answer", sdp: answer });
  appendLog("realtime", "WebRTC session established.");
}

function waitForDataChannelOpen(timeoutMs = 5000) {
  if (dataChannel && dataChannel.readyState === "open") {
    return Promise.resolve();
  }
  return new Promise((resolve, reject) => {
    const timeout = setTimeout(() => {
      reject(new Error("Data channel not ready."));
    }, timeoutMs);
    const handleOpen = () => {
      clearTimeout(timeout);
      dataChannel.removeEventListener("open", handleOpen);
      resolve();
    };
    dataChannel.addEventListener("open", handleOpen);
  });
}

function configureManualResponses() {
  if (manualResponseConfigSent) {
    return;
  }
  if (!dataChannel || dataChannel.readyState !== "open") {
    return;
  }
  const detection = sessionSettings.turnDetection || "server_vad";
  dataChannel.send(
    JSON.stringify({
      type: "session.update",
      session: {
        turn_detection: {
          type: detection,
          create_response: false,
        },
      },
    })
  );
  manualResponseConfigSent = true;
}

function handleRealtimeEvent(event) {
  let data = null;
  try {
    data = JSON.parse(event.data);
  } catch (err) {
    appendLog("realtime", "Non-JSON event received.");
    return;
  }

  if (data.type === "conversation.item.input_audio_transcription.completed") {
    const transcript = data.transcript || "";
    if (transcript.trim()) {
      document.getElementById("user_transcript").textContent = transcript;
      appendLog("realtime", "User transcript completed.");
      handleUserTranscript(transcript);
    }
  } else if (data.type === "conversation.item.input_audio_transcription.delta") {
    const partial = data.transcript || "";
    if (partial.trim()) {
      document.getElementById("user_transcript").textContent = partial;
    }
  } else if (data.type === "response.audio_transcript.delta" || data.type === "response.output_text.delta") {
    assistantTranscriptBuffer += data.delta || "";
    document.getElementById("assistant_transcript").textContent = assistantTranscriptBuffer;
  } else if (data.type === "response.audio_transcript.done" || data.type === "response.output_text.done") {
    if (assistantTranscriptBuffer.trim()) {
      document.getElementById("assistant_transcript").textContent = assistantTranscriptBuffer;
      if (!suppressAssistantAppend) {
        appendAssistantTranscript(assistantTranscriptBuffer);
      } else {
        suppressAssistantAppend = false;
      }
      assistantTranscriptBuffer = "";
    }
  }
}

async function handleUserTranscript(transcript) {
  const ackResponse = await fetch(`/${session.agentId}/acknowledge`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json; charset=utf-8",
    },
    body: JSON.stringify({ content: transcript }),
  });
  if (!ackResponse.ok) {
    appendLog("promise", "acknowledge failed.");
    return;
  }
  const data = await fetchPromptBundle();
  setActiveStatus(data.active);
  applyPromptBundle(data, true);
}

async function appendAssistantTranscript(transcript) {
  const response = await fetch(`/${session.agentId}/assistant`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json; charset=utf-8",
    },
    body: JSON.stringify({ content: transcript }),
  });
  if (!response.ok) {
    appendLog("promise", "assistant append failed.");
    return;
  }
  appendLog("promise", "Assistant transcript stored.");
}

async function resetAgent() {
  if (!confirm("Reset the conversation?")) {
    return;
  }
  const response = await fetch(`/${session.agentId}/reset`, {
    method: "DELETE",
  });
  if (!response.ok) {
    appendLog("app", "Reset failed.");
    return;
  }
  const data = await response.json();
  setActiveStatus(data.active);
  document.getElementById("user_transcript").textContent = "";
  document.getElementById("assistant_transcript").textContent = "";
  assistantTranscriptBuffer = "";
  try {
    const promptBundle = await fetchPromptBundle();
    sendSessionUpdate(buildSystemPrompt(promptBundle));
  } catch (error) {
    appendLog("app", "Unable to refresh prompt after reset.");
  }
  if (data.assistantResponse && data.assistantResponse.text) {
    speakStoredAssistantUtterance(data.assistantResponse.text);
  }
}

function sendSessionUpdate(systemPrompt, settings = null) {
  if (!dataChannel || dataChannel.readyState !== "open") {
    appendLog("realtime", "Data channel not ready for session update.");
    return;
  }
  lastSystemPrompt = systemPrompt || "";
  const sessionPayload = {
    instructions: lastSystemPrompt,
  };
  if (settings) {
    if (settings.voice) {
      sessionPayload.voice = settings.voice;
    }
    if (typeof settings.temperature === "number" && !Number.isNaN(settings.temperature)) {
      sessionPayload.temperature = settings.temperature;
    }
    if (settings.turnDetection) {
      sessionPayload.turn_detection = {
        type: settings.turnDetection,
        create_response: false,
      };
    }
  }
  dataChannel.send(
    JSON.stringify({
      type: "session.update",
      session: sessionPayload,
    })
  );
  appendLog("realtime", "Session instructions updated.");
}

function applySessionSettings() {
  if (!dataChannel || dataChannel.readyState !== "open") {
    return;
  }
  sendSessionUpdate(lastSystemPrompt, sessionSettings);
}

function updatePushToTalkUi() {
  const manualControls = document.getElementById("manual_controls");
  const pushToTalkButton = document.getElementById("push_to_talk");
  const isManual = sessionSettings.turnDetection === "none";
  if (manualControls) {
    manualControls.classList.toggle("d-none", !isManual);
  }
  if (pushToTalkButton) {
    pushToTalkButton.disabled = !session.isListening || !isManual;
    if (!session.isListening || !isManual) {
      pushToTalkButton.classList.remove("is-pressed");
    }
  }
  if (session.isListening) {
    if (isManual) {
      setMicEnabled(false);
    } else {
      setMicEnabled(true);
    }
  }
}

function startPushToTalk(event) {
  if (event) {
    event.preventDefault();
  }
  const button = document.getElementById("push_to_talk");
  if (!session.isListening || sessionSettings.turnDetection !== "none" || pushToTalkActive) {
    return;
  }
  pushToTalkActive = true;
  if (button) {
    button.classList.add("is-pressed");
  }
  setMicEnabled(true);
}

function stopPushToTalk(event) {
  if (event) {
    event.preventDefault();
  }
  const button = document.getElementById("push_to_talk");
  if (!pushToTalkActive) {
    return;
  }
  pushToTalkActive = false;
  if (button) {
    button.classList.remove("is-pressed");
  }
  setMicEnabled(false);
  commitManualTurn();
}

function commitManualTurn() {
  if (!dataChannel || dataChannel.readyState !== "open") {
    return;
  }
  dataChannel.send(
    JSON.stringify({
      type: "input_audio_buffer.commit",
    })
  );
}

function setMicEnabled(enabled) {
  if (!micStream) {
    return;
  }
  micStream.getAudioTracks().forEach((track) => {
    track.enabled = enabled;
  });
}

function sendResponseCreate(instructions) {
  if (!dataChannel || dataChannel.readyState !== "open") {
    appendLog("realtime", "Data channel not ready for response.create.");
    return;
  }
  dataChannel.send(
    JSON.stringify({
      type: "response.create",
      response: {
        instructions: instructions,
        modalities: ["audio", "text"],
      },
    })
  );
  appendLog("realtime", "Initial response triggered.");
}

function appendLog(source, message) {
  const prefix = source ? `[${source}] ` : "";
  console.log(`${prefix}${message}`);
}

function getLastAssistantUtterance(conversation) {
  if (!Array.isArray(conversation) || conversation.length === 0) {
    return null;
  }
  const last = conversation[conversation.length - 1];
  if (!last || last.role !== "assistant") {
    return null;
  }
  return last.content || null;
}

function speakStoredAssistantUtterance(text) {
  if (!text) {
    return;
  }
  suppressAssistantAppend = true;
  sendResponseCreate(`Read the following message verbatim: ${text}`);
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
