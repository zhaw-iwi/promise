package ch.zhaw.statefulconversation.controllers.views;

public class MonitorStateView {
    private AgentStateInfoView state;
    private boolean active;

    public MonitorStateView(AgentStateInfoView state, boolean active) {
        this.state = state;
        this.active = active;
    }

    public AgentStateInfoView getState() {
        return this.state;
    }

    public boolean isActive() {
        return this.active;
    }
}
