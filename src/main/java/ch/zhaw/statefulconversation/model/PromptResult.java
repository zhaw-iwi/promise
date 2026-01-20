package ch.zhaw.statefulconversation.model;

import java.util.List;

public class PromptResult {
    private final String stateName;
    private final String systemPrompt;
    private final String starterPrompt;
    private final List<Utterance> conversation;

    public PromptResult(State state, String systemPrompt, String starterPrompt, List<Utterance> conversation) {
        this.stateName = state.getName();
        this.systemPrompt = systemPrompt;
        this.starterPrompt = starterPrompt;
        this.conversation = conversation;
    }

    public String getStateName() {
        return stateName;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String getStarterPrompt() {
        return starterPrompt;
    }

    public List<Utterance> getConversation() {
        return conversation;
    }
}
