package ch.zhaw.statefulconversation.controllers.views;

public class RealtimeSessionView {
    private String clientSecret;
    private String model;
    private String realtimeUrl;

    public RealtimeSessionView(String clientSecret, String model, String realtimeUrl) {
        this.clientSecret = clientSecret;
        this.model = model;
        this.realtimeUrl = realtimeUrl;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getModel() {
        return model;
    }

    public String getRealtimeUrl() {
        return realtimeUrl;
    }
}
