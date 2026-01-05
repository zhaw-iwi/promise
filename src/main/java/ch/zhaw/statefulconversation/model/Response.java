package ch.zhaw.statefulconversation.model;

public class Response {

    private String stateName;
    private String text;

    public Response(State state, String text) {
        this.stateName = state.getName();
        this.text = text;
    }

    public Response(State state) {
        this(state, null);
    }

    public String getStateName() {
        return stateName;
    }

    public String getText() {
        return text;
    }
}
