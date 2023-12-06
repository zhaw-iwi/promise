package ch.zhaw.statefulconversation.views;

import java.util.List;

public class ResponseView {
    private List<String> assistantSays;
    private boolean isActive;

    public ResponseView(List<String> assistantSays, boolean isActive) {
        this.assistantSays = assistantSays;
        this.isActive = isActive;
    }

    public List<String> getAssistantSays() {
        return this.assistantSays;
    }

    public boolean isActive() {
        return this.isActive;
    }
}
