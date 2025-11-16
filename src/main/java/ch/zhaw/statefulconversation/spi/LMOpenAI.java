package ch.zhaw.statefulconversation.spi;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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
    private static final String REMINDER_EXTRACTION = """
            Return valid JSON data that can be parsed with the GSON library for Java.
            If the value extracted is of type string, ensure it is enclosed in double quotes.
            If your response is a JSON object, ensure it starts and ends with curly brackets.
            If your response is a JSON list, ensure it starts and ends with square brackets.
            Return only the raw JSON text without any markdown formatting (do not include triple backticks), explanations, or additional text.
            """;
    private static final String REMINDER_SUMMARISATION = "Remember to reply with the summary in JSON format only so that it can be parsed with a Java program using the GSON library.";

    public static String complete(Utterances utterances, String systemPrepend, String stateName) {
        List<Utterance> totalPrompt = LMOpenAI.composePrompt(utterances, systemPrepend, stateName);
        LMOpenAI.LOGGER.info("LMOpenAI.complete() with " + totalPrompt);
        String result = LMOpenAI.openai(totalPrompt);
        return result;
    }

    public static String complete(Utterances utterances, String systemPrepend, String systemAppend, String stateName) {
        List<Utterance> totalPrompt = LMOpenAI.composePrompt(utterances, systemPrepend, systemAppend, stateName); // Corrected
                                                                                                                  // call
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
        return Boolean.parseBoolean(response);
    }

    public static JsonElement extract(Utterances utterances, String systemPrepend) {
        if (utterances.isEmpty()) {
            throw new RuntimeException("cannot extract from empty utterances");
        }
        List<Utterance> totalPrompt = LMOpenAI.composePromptCondensed(utterances, systemPrepend,
                LMOpenAI.REMINDER_EXTRACTION);
        LMOpenAI.LOGGER.info("LMOpenAI.extract() with " + totalPrompt);
        String response = LMOpenAI.openai(totalPrompt, 0.0f, 0.0f);
        return new Gson().fromJson(response, JsonElement.class);
    }

    public static JsonElement summarise(Utterances utterances, String systemPrepend) {
        if (utterances.isEmpty()) {
            throw new RuntimeException("cannot summarise from empty utterance");
        }
        List<Utterance> totalPrompt = LMOpenAI.composePromptCondensed(utterances, systemPrepend,
                LMOpenAI.REMINDER_SUMMARISATION);
        LMOpenAI.LOGGER.info("LMOpenAI.summarise() with " + totalPrompt);
        String response = LMOpenAI.openai(totalPrompt, 0.0f, 0.0f);
        return new Gson().fromJson(response, JsonElement.class);
    }

    public static String summariseOffline(Utterances utterances, String systemPrepend) {
        if (utterances.isEmpty()) {
            throw new RuntimeException("cannot summarise offline from empty utterance");
        }
        List<Utterance> totalPrompt = LMOpenAI.composePromptCondensed(utterances, systemPrepend);
        LMOpenAI.LOGGER.info("LMOpenAI.summariseOffline() with " + totalPrompt);
        String result = LMOpenAI.openai(totalPrompt, 0.0f, 0.0f);
        return result;
    }

    private static List<Utterance> composePrompt(Utterances utterances, String systemPrepend, String stateName) {
        List<Utterance> result = new ArrayList<Utterance>();
        if (systemPrepend == null) {
            throw new NullPointerException(systemPrepend + " systemPrepend (Decision prompt) cannot be null.");
        }
        result.add(new Utterance("system", systemPrepend, stateName));
        result.addAll(utterances.toList());
        return result;
    }

    private static List<Utterance> composePrompt(Utterances utterances, String systemPrepend, String systemAppend,
            String stateName) {
        List<Utterance> result = new ArrayList<>();
        if (systemPrepend == null) {
            throw new NullPointerException("systemPrepend (Decision prompt) cannot be null.");
        }
        result.add(new Utterance("system", systemPrepend, stateName));
        result.addAll(utterances.toList());
        if (systemAppend != null) {
            result.add(new Utterance("system", systemAppend, stateName));
        }
        return result;
    }

    private static List<Utterance> composePromptCondensed(Utterances utterances, String systemPrepend) {
        List<Utterance> result = new ArrayList<>();
        if (systemPrepend == null) {
            throw new NullPointerException("systemPrepend (Decision prompt) cannot be null.");
        }
        result.add(new Utterance("system", systemPrepend, null)); // Check whether this is the best way to handle the
                                                                  // absence of stateName
        result.add(new Utterance("system", "<conversation>" + utterances.toString() + "</conversation>", null));
        return result;
    }

    private static List<Utterance> composePromptCondensed(Utterances utterances, String systemPrepend,
            String systemAppend) {
        List<Utterance> result = composePromptCondensed(utterances, systemPrepend);
        if (systemAppend == null) {
            throw new NullPointerException("systemAppend cannot be null.");
        }
        result.add(new Utterance("system", systemAppend, null)); // Check whether this is the best way to handle the
                                                                 // absence of stateName
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

            Instant start = Instant.now();

            JsonObject payload = OpenAIProperties.instance().payload();
            payload.addProperty("temperature", temperature);
            payload.addProperty("top_p", topP);
            payload.add("messages", LMOpenAI.GSON.toJsonTree(message));

            // @TODO seems to be available in azure.openai
            // payload.addProperty("max_tokens", 800);
            // payload.addProperty("frequency_penalty", 0);
            // payload.addProperty("presence_penalty", 0);
            // payload.addProperty("stop", null);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(OpenAIProperties.instance().getUrl()))
                    .header(OpenAIProperties.instance().headerKeyNameForAPIKey(), OpenAIProperties.instance().getKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(LMOpenAI.GSON.toJson(payload)))
                    .build();
            HttpResponse<String> response = LMOpenAI.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            Instant end = Instant.now();
            LMOpenAI.LOGGER.info(
                    "LMOpenAI.openai() http request took " + Duration.between(start, end).toMillis() + " milliseconds");

            // @todo: possibly do some more extensive testing here?
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException(
                        "unable to use openai api - http request returned status code: " + response.statusCode()
                                + " (\n\t"
                                + response.body() + "\n\t" + response.toString() + "\n)");
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

        if (jsonChoice.has("finish_reason") && "content_filter".equals(jsonChoice.get("finish_reason").getAsString())) {
            throw new ContenFilterException(
                    "unable to use openai api - content of message was filtered: " + jsonResponse);

        }

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