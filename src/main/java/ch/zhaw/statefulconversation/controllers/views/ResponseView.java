package ch.zhaw.statefulconversation.controllers.views;

import ch.zhaw.statefulconversation.model.Response;

public class ResponseView {
    private Response assistantResponse;
    private boolean isActive;

    public ResponseView(Response assistantResponse, boolean isActive) {
        this.assistantResponse = assistantResponse;
        this.isActive = isActive;
    }

    public Response getAssistantResponse() {
        return this.assistantResponse;
    }

    public boolean isActive() {
        return this.isActive;
    }
}
