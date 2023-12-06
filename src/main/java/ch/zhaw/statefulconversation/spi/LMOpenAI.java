package ch.zhaw.statefulconversation.spi;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.zhaw.statefulconversation.model.Utterance;
import ch.zhaw.statefulconversation.model.Utterances;

public class LMOpenAI {
    private static final Logger LOGGER = LoggerFactory.getLogger(LMOpenAI.class);

    private static final String REMINDER_DECISION = "Remember to reply with either true or false only so that it can be parsed with the Java programming language. Your answer needs to work with Boolean.parseBoolean() method, which only accepts English true or false.";
    private static final String REMINDER_EXTRACTION = "Remember to reply with the extracted value in JSON format only so that it can be parsed with a Java program using the GSON library.";

    public static String complete(Utterances utterances, String systemPrepend) {
        List<Utterance> totalPrompt = LMOpenAI.composePrompt(utterances, systemPrepend);
        LMOpenAI.LOGGER.info("LMOpenAI.complete() with " + totalPrompt);
        String result = LMOpenAI.openai(totalPrompt);
        return result;
    }

    public static String complete(Utterances utterances, String systemPrepend, String systemAppend) {
        List<Utterance> totalPrompt = LMOpenAI.composePrompt(utterances, systemPrepend, systemAppend);
        LMOpenAI.LOGGER.info("LMOpenAI.complete() with " + totalPrompt);
        String result = LMOpenAI.openai(totalPrompt);
        return result;
    }

    public static boolean decide(Utterances utterances, String systemPrepend) {
        if (utterances.isEmpty()) {
            throw new RuntimeException("cannot decide about empty utterances");
        }
        List<Utterance> totalPrompt = LMOpenAI.composePromptCondensed(utterances, systemPrepend,
                LMOpenAI.REMINDER_DECISION);
        LMOpenAI.LOGGER.info("LMOpenAI.decide() with " + totalPrompt);
        String response = LMOpenAI.openai(totalPrompt, 0.0f, 0.0f);
        boolean result = Boolean.parseBoolean(response);
        return result;
    }

    public static JsonElement extract(Utterances utterances, String systemPrepend) {
        if (utterances.isEmpty()) {
            throw new RuntimeException("cannot extract from empty utterances");
        }
        List<Utterance> totalPrompt = LMOpenAI.composePromptCondensed(utterances, systemPrepend,
                LMOpenAI.REMINDER_EXTRACTION);
        LMOpenAI.LOGGER.info("LMOpenAI.extract() with " + totalPrompt);
        String response = LMOpenAI.openai(totalPrompt, 0.0f, 0.0f);
        Gson gson = new Gson();
        JsonElement result = gson.fromJson(response, JsonElement.class);
        return result;
    }

    public static String summarise(Utterances utterances, String systemPrepend) {
        if (utterances.isEmpty()) {
            throw new RuntimeException("cannot summarise from empty utterance");
        }
        List<Utterance> totalPrompt = LMOpenAI.composePrompt(utterances, systemPrepend);
        LMOpenAI.LOGGER.info("LMOpenAI.summarise() with " + totalPrompt);
        String result = LMOpenAI.openai(totalPrompt, 0.0f, 0.0f);
        return result;
    }

    private static List<Utterance> composePrompt(Utterances utterances, String systemPrepend) {
        List<Utterance> result = new ArrayList<Utterance>();
        if (systemPrepend == null) {
            throw new NullPointerException(systemPrepend + " systemPrepend (Decision prompt) cannot be null.");
        }
        result.add(new Utterance("system", systemPrepend));
        result.addAll(utterances.toList());
        return result;
    }

    private static List<Utterance> composePrompt(Utterances utterances, String systemPrepend, String systemAppend) {
        if (systemPrepend == null) {
            throw new NullPointerException(systemPrepend + " systemPrepend (Decision prompt) cannot be null.");
        }
        List<Utterance> result = LMOpenAI.composePrompt(utterances, systemPrepend);
        if (systemAppend == null) {
            throw new NullPointerException(systemAppend + " systemAppend cannot be null.");
        }
        result.add(new Utterance("system", systemAppend));
        return result;
    }

    private static List<Utterance> composePromptCondensed(Utterances utterances, String systemPrepend,
            String systemAppend) {
        List<Utterance> result = new ArrayList<Utterance>();
        if (systemPrepend == null) {
            throw new NullPointerException(systemPrepend + " systemPrepend (Decision prompt) cannot be null.");
        }
        result.add(new Utterance("system", systemPrepend));
        result.add(new Utterance("user", utterances.toString()));
        if (systemAppend == null) {
            throw new NullPointerException(systemAppend + " systemAppend cannot be null.");
        }
        result.add(new Utterance("system", systemAppend));
        return result;
    }

    private static String openai(List<Utterance> messages) {
        return LMOpenAI.openai(messages, 1, 1);
    }

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new GsonBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(GsonExclude.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return clazz == Instant.class;
        }

    }).create();

    public static String openai(List<Utterance> message, float temperature, float topP) {
        try {

            JsonObject payload = new JsonObject();
            payload.addProperty("model", OpenAIProperties.instance().getModel());
            payload.addProperty("temperature", temperature);
            payload.addProperty("top_p", topP);
            payload.add("messages", LMOpenAI.GSON.toJsonTree(message));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(OpenAIProperties.instance().getUrl()))
                    .header("Authorization", "Bearer " + OpenAIProperties.instance().getKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(LMOpenAI.GSON.toJson(payload)))
                    .build();
            HttpResponse<String> response = LMOpenAI.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            // @todo: possibly do some more extensive testing here?
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException(
                        "unable to use openai api - http request returned status code: " + response.statusCode());
            }

            JsonObject jsonResponse = LMOpenAI.GSON.fromJson(response.body(), JsonObject.class);
            String result = LMOpenAI.testAndObtainContent(jsonResponse);
            LMOpenAI.LOGGER.info("LMOpenAI.openai() returns " + result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("unable to request openai :-(", e);
        }
    }

    private static String testAndObtainContent(JsonObject jsonResponse) {
        if (!jsonResponse.has("choices")) {
            throw new RuntimeException(
                    "unable to use openai api - json response has no choices: " + jsonResponse);
        }

        JsonArray jsonChoices = jsonResponse.getAsJsonArray("choices");

        if (jsonChoices.size() == 0) {
            throw new RuntimeException(
                    "unable to use openai api - json choices is empty: " + jsonResponse);
        }

        JsonObject jsonChoice = jsonChoices.get(0).getAsJsonObject();

        if (!jsonChoice.has("message")) {
            throw new RuntimeException(
                    "unable to use openai api - json choices is empty: " + jsonResponse);
        }

        JsonObject jsonMessage = jsonChoice.get("message").getAsJsonObject();

        if (!jsonMessage.has("content")) {
            throw new RuntimeException(
                    "unable to use openai api - json message has no content: " + jsonResponse);
        }

        return jsonMessage.get("content").getAsString();
    }
}