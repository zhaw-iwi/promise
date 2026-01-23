package ch.zhaw.statefulconversation.model;

import java.util.List;

public class PromptResult {
    private final String stateName;
    private final String systemPrompt;
    private final List<Utterance> conversation;
    private final boolean starting;

    public PromptResult(State state, String systemPrompt, List<Utterance> conversation) {
        this.stateName = state.getName();
        this.systemPrompt = systemPrompt;
        this.conversation = conversation;
        this.starting = state.isStarting();
    }

    public String getStateName() {
        return stateName;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public List<Utterance> getConversation() {
        return conversation;
    }

    public boolean isStarting() {
        return starting;
    }
}
