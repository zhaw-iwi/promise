package ch.zhaw.statefulconversation.spi;

public class RealtimeSessionInfo {
    private final String clientSecret;
    private final String model;
    private final String realtimeUrl;

    public RealtimeSessionInfo(String clientSecret, String model, String realtimeUrl) {
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
