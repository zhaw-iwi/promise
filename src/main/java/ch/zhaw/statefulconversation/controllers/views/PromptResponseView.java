package ch.zhaw.statefulconversation.controllers.views;

import java.util.List;

import ch.zhaw.statefulconversation.model.PromptResult;
import ch.zhaw.statefulconversation.model.Utterance;

public class PromptResponseView {
    private String stateName;
    private String systemPrompt;
    private String starterPrompt;
    private List<Utterance> conversation;
    private boolean isActive;

    public PromptResponseView(PromptResult promptResult, boolean isActive) {
        this.stateName = promptResult.getStateName();
        this.systemPrompt = promptResult.getSystemPrompt();
        this.starterPrompt = promptResult.getStarterPrompt();
        this.conversation = promptResult.getConversation();
        this.isActive = isActive;
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

    public boolean isActive() {
        return isActive;
    }
}
