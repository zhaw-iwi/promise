package ch.zhaw.statefulconversation.controllers.views;

public class AgentStateInfoView {
    private String name;

    public AgentStateInfoView(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
