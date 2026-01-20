package ch.zhaw.statefulconversation.controllers.views;

public class AgentStateInfoView {
    private String name;
    private String innerName;
    private java.util.List<String> innerNames;

    public AgentStateInfoView(String name, String innerName, java.util.List<String> innerNames) {
        this.name = name;
        this.innerName = innerName;
        this.innerNames = innerNames;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInnerName() {
        return innerName;
    }

    public void setInnerName(String innerName) {
        this.innerName = innerName;
    }

    public java.util.List<String> getInnerNames() {
        return innerNames;
    }

    public void setInnerNames(java.util.List<String> innerNames) {
        this.innerNames = innerNames;
    }
}
