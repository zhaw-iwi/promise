package ch.zhaw.statefulconversation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import ch.zhaw.statefulconversation.spi.OpenAIProperties;

@SpringBootApplication
@EnableConfigurationProperties(OpenAIProperties.class)
public class StatefulconversationApplication {

	public static void main(String[] args) {
		SpringApplication.run(StatefulconversationApplication.class, args);
	}
}
