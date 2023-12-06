package ch.zhaw.statefulconversation.spi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:openai.properties")
@ConfigurationProperties(prefix = "openai")
public class OpenAIProperties {

    private static OpenAIProperties INSTANCE;

    public static OpenAIProperties instance() {
        return OpenAIProperties.INSTANCE;
    }

    private String url;
    private String model;
    private String key;

    public OpenAIProperties() {
        OpenAIProperties.INSTANCE = this;
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

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
