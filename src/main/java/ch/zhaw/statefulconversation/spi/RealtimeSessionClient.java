package ch.zhaw.statefulconversation.spi;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class RealtimeSessionClient {

    private static final String DEFAULT_REALTIME_SESSION_URL = "https://api.openai.com/v1/realtime/sessions";
    private static final String DEFAULT_REALTIME_URL = "https://api.openai.com/v1/realtime";
    private static final String DEFAULT_REALTIME_MODEL = "gpt-4o-realtime-preview-2024-12-17";

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();

    public static RealtimeSessionInfo createSession() {
        OpenAIProperties props = OpenAIProperties.instance();
        if (!"openai".equals(props.getOpenaivsazureopenai())) {
            throw new RuntimeException("realtime session creation is only supported for openai at the moment");
        }

        String model = props.getRealtimeModel() != null ? props.getRealtimeModel() : DEFAULT_REALTIME_MODEL;
        String sessionUrl = props.getRealtimeSessionUrl() != null ? props.getRealtimeSessionUrl()
                : DEFAULT_REALTIME_SESSION_URL;
        String realtimeUrl = props.getRealtimeUrl() != null ? props.getRealtimeUrl() : DEFAULT_REALTIME_URL;

        JsonObject payload = new JsonObject();
        payload.addProperty("model", model);
        payload.add("modalities", GSON.toJsonTree(new String[] { "audio", "text" }));
        JsonObject transcription = new JsonObject();
        transcription.addProperty("model", "whisper-1");
        payload.add("input_audio_transcription", transcription);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(sessionUrl))
                    .header(props.headerKeyNameForAPIKey(), props.getKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(payload)))
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException(
                        "unable to create realtime session - http request returned status code: " + response.statusCode()
                                + " (\n\t"
                                + response.body() + "\n\t" + response.toString() + "\n)");
            }

            JsonObject jsonResponse = GSON.fromJson(response.body(), JsonObject.class);
            JsonObject clientSecret = jsonResponse.getAsJsonObject("client_secret");
            if (clientSecret == null || !clientSecret.has("value")) {
                throw new RuntimeException("realtime session response missing client_secret.value: " + jsonResponse);
            }
            String clientSecretValue = clientSecret.get("value").getAsString();
            return new RealtimeSessionInfo(clientSecretValue, model, realtimeUrl);
        } catch (Exception e) {
            throw new RuntimeException("unable to create realtime session", e);
        }
    }
}
