# CONTEXT.md  

PROMISE – State-Machine-Driven Framework for Language-Based Interaction

## 1. Vision and Motivation

PROMISE is a framework for engineering complex, controllable, language-based interactions using explicit state-machine modeling and structured prompt orchestration.

Large language models enable highly capable conversational behavior. However, without architectural control, such behavior becomes:

- Hard to predict  
- Difficult to test  
- Difficult to constrain  
- Difficult to integrate with software systems  

PROMISE addresses this by embedding language models inside a deterministic interaction control structure.

The framework is intentionally focused on:

- Turn-based interaction  
- Verbal input and output  
- Explicit conversational flow control  
- Structured prompt composition  
- Inspectable transition logic  

PROMISE enables the development of conversational agents that remain controllable, testable, and composable while leveraging the generative power of language models.

---

## 2. Core Design Objectives

PROMISE is designed to achieve the following objectives:

1. Preserve explicit state-machine control over conversational flow  
2. Maintain deterministic orchestration around non-deterministic language models  
3. Enable compositional prompt engineering  
4. Separate conversational structure from language generation  
5. Provide explicit shared interaction memory  
6. Support inspection, traceability, and regression testing  
7. Minimize architectural complexity for purely verbal systems  

The framework must allow developers to build agents that:

- Conduct structured multi-step interactions  
- Transition between conversational regimes based on semantic evaluation  
- Store and reuse structured information across states  
- Integrate with external systems  
- Remain predictable at the control level  

---

## 3. Conceptual Model

### 3.1 Explicit State-Machine Control

Primary interaction logic is modeled as explicit state machines.

- Each state represents a conversational regime  
- Transitions connect states  
- Transitions are governed by structured decisions  
- Actions execute upon transition  
- Nested state machines are supported via outer states  

The state machine is the sole authority over interaction flow.

Language models never directly control transitions.  
They generate content and semantic evaluations inside state-defined boundaries.

---

### 3.2 Turn-Based Interaction

PROMISE operates on a turn-based model.

Each interaction step consists of:

1. User input  
2. Evaluation of transitions  
3. Execution of transition actions  
4. Generation of assistant response  

Continuous event-driven control is outside the architectural scope of PROMISE.  
Conversation turns are the primary unit of interaction.

---

### 3.3 Structured Prompt Orchestration

PROMISE decomposes prompt logic into structured components.

These may include:

- State prompts  
- Starter prompts  
- Transition decision prompts  
- Action prompts  
- Outer-state prompts  

Prompts are composed deterministically by the framework based on the active state and transition logic.

This compositional structure:

- Improves controllability  
- Increases reliability  
- Enables reuse of conversational fragments  
- Reduces hidden control flow inside prompts  

A single-state agent using one state prompt is valid.  
However, complex interactions should use multiple composable prompt elements.

---

### 3.4 Decisions and Actions

Transitions are governed by structured decision elements.

Decisions evaluate conversational context and produce structured outcomes that determine whether transitions occur.

Actions execute upon transition and may:

- Extract structured information  
- Update storage  
- Produce summaries  
- Trigger side effects  

The evaluation logic is prompt-driven but structurally governed by the state machine.

---

### 3.5 Explicit Interaction Storage

PROMISE provides a key-value storage system attached to each agent.

Storage:

- Persists across states  
- Is explicitly referenced by states and transitions  
- Enables cross-state memory  
- Enables integration with external systems  

Storage values are structured and inspectable.

All memory beyond the current conversational turn must pass through this explicit storage layer.

---

### 3.6 Agent Abstraction

An Agent encapsulates:

- The state machine  
- Storage  
- Interaction history  
- Prompt orchestration  

The agent is the sole external interface.

All interaction occurs through:

- Start  
- Respond  
- Reset  
- Summarise  
- Realtime integration endpoints  

This preserves encapsulation and architectural clarity.

---

### 3.7 Realtime Execution Mode

PROMISE supports realtime speech interaction.

In realtime mode:

- PROMISE assembles structured prompt bundles  
- External speech models generate spoken output  
- Transcripts are fed back into PROMISE  
- Transition logic remains unchanged  

Realtime integration does not alter architectural control semantics.

The state machine remains authoritative.

---

## 4. What PROMISE Is

PROMISE is:

- A state-machine-driven conversational orchestration framework  
- A structured prompt composition system  
- A deterministic control layer around language models  
- A framework for engineering multi-step verbal interactions  
- Suitable for bilateral and multilateral conversational agents  
- Designed for inspectability and testability  

---

## 5. What PROMISE Is Not

PROMISE is not:

- A free-form chatbot wrapper  
- A purely prompt-engineering toolkit without structural control  
- An event-driven autonomy framework  
- A multimodal robotics control system  
- A reinforcement learning system  

Its domain is structured, turn-based, verbal interaction.

---

## 6. Canonical Use Cases and Scenarios

The following scenarios define intended capability coverage.

### 6.1 Single-State Check-In

A digital agent conducts a structured check-in interaction using a single state.

Characteristics:

- Single state  
- Single transition to final  
- Structured summarization action  

Purpose:

- Minimal viable configuration  
- Demonstrates prompt orchestration and storage  

---

### 6.2 Multi-State Persuasion Flow

An agent:

1. Identifies a problem  
2. Explores reasons  
3. Offers options  
4. Confirms commitment  

Characteristics:

- Sequential states  
- Structured transitions  
- Cross-state storage usage  

Purpose:

- Demonstrates multi-step flow control  
- Shows compositional prompts  

---

### 6.3 Nested Outer-State Supervision

An outer state supervises inner conversational states.

Characteristics:

- Hierarchical state machines  
- Outer-level prompts applied globally  
- Global exit transitions  

Purpose:

- Demonstrates layered conversational control  

---

### 6.4 Retrieval-Augmented Interaction

An agent queries an external knowledge source and integrates results into prompts.

Characteristics:

- Action-based extraction  
- Storage injection  
- Prompt augmentation  

Purpose:

- Demonstrates system integration  

---

### 6.5 Realtime Speech Interaction

An agent conducts a voice-based interaction using realtime streaming.

Characteristics:

- Prompt bundle generation  
- Transcript-driven transition evaluation  
- Speech output externalized  

Purpose:

- Demonstrates architecture stability under realtime execution  

---

## 7. High-Level Requirements

### 7.1 Architectural Requirements

- Explicit state-machine control  
- Deterministic prompt orchestration  
- Encapsulated agent abstraction  
- Explicit storage layer  

### 7.2 Prompt Requirements

- Prompts must remain compositional  
- Prompt structure must be governed by state context  
- Hidden control flow inside prompt text must be avoided  

### 7.3 Storage Requirements

- All persistent conversational memory must use storage  
- Storage must remain inspectable and bounded  

### 7.4 Transition Requirements

- Transitions must be explicit  
- Transition logic must not be embedded in assistant responses  
- Decision outcomes must be traceable  

### 7.5 Realtime Requirements

- Realtime execution must preserve state-machine authority  
- Speech rendering must not alter transition semantics  

### 7.6 Developer Experience Requirements

- Minimal boilerplate for simple agents  
- Clear separation of state definition and runtime execution  
- Support for monitoring and introspection  

### 7.7 Soft Safety and Traceability

PROMISE emphasizes:

- Inspectable control flow  
- Deterministic orchestration structure  
- Clear authority boundaries  
- Traceable transitions and actions  

This supports controlled experimentation and iterative development.

---

## 8. Acceptance Criterion

PROMISE is architecturally coherent when:

- Multi-state conversational agents can be implemented without altering core abstractions  
- Realtime and non-realtime modes use the same control semantics  
- State-machine authority over interaction flow remains intact  
- Prompt composition remains structured and deterministic  
- Storage enables cross-state continuity without hidden memory  

The state machine must remain the ultimate authority over conversational progression.
