package ch.zhaw.statefulconversation.spi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.google.gson.JsonObject;

@Configuration
@PropertySource("classpath:openai.properties")
@ConfigurationProperties(prefix = "openai")
public class OpenAIProperties {

    private static OpenAIProperties INSTANCE;

    public static OpenAIProperties instance() {
        return OpenAIProperties.INSTANCE;
    }

    private static final String OPENAI = "openai";
    private static final String AZUREOPENAI = "azureopenai";

    private String openaivsazureopenai;
    private String url;
    private String model;
    private String key;

    public OpenAIProperties() {
        OpenAIProperties.INSTANCE = this;
    }

    public String getOpenaivsazureopenai() {
        return this.openaivsazureopenai;
    }

    public void setOpenaivsazureopenai(String openaivsazureopenai) {
        this.openaivsazureopenai = openaivsazureopenai;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String headerKeyNameForAPIKey() {
        if (OpenAIProperties.OPENAI.equals(this.getOpenaivsazureopenai())) {
            return "Authorization";
        }
        if (OpenAIProperties.AZUREOPENAI.equals(this.getOpenaivsazureopenai())) {
            return "api-key";
        }
        throw new RuntimeException(
                "unexpected value for property openaivsazureopenai: " + this.getOpenaivsazureopenai());
    }

    public String getKey() {
        if (OpenAIProperties.OPENAI.equals(this.getOpenaivsazureopenai())) {
            return "Bearer " + this.key;
        }
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public JsonObject payload() {
        JsonObject result = new JsonObject();
        if (OpenAIProperties.OPENAI.equals(this.getOpenaivsazureopenai())) {
            result.addProperty("model", this.getModel());
        }
        return result;
    }
}
