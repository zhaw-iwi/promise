package ch.zhaw.statefulconversation.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.zhaw.statefulconversation.spi.LMOpenAI;
import ch.zhaw.statefulconversation.spi.OpenAIProperties;

@SpringBootTest
class LMOpenAITests {

	@Test
	void lMOpenAIProperties() {
		assertInstanceOf(String.class, OpenAIProperties.instance().getUrl());
		assertInstanceOf(String.class, OpenAIProperties.instance().getKey());
	}

	@Test
	void lMOpenAIBase() {
		int numberEpxected = 3;

		List<Utterance> messages = List.of(
				new Utterance("system", "Return the integer value " + numberEpxected + "."));
		String response = LMOpenAI.openai(messages, 0.7f, 1.0f);
		assertNotNull(response);
		assertDoesNotThrow(new ThrowingSupplier<Integer>() {

			@Override
			public Integer get() throws Throwable {
				return Integer.parseInt(response);
			}

		}, new ObjectSerialisationSupplier(response));
		int numberReceived = Integer.parseInt(response);
		assertEquals(numberEpxected, numberReceived, new ObjectSerialisationSupplier(response));
	}

	@Test
	void lMOpenAIComplete() {
		int numberEpxected = 3;

		Utterances utterances = new Utterances();
		String response = LMOpenAI.complete(utterances, "Return the integer value " + numberEpxected + ".");
		assertNotNull(response);
		assertDoesNotThrow(new ThrowingSupplier<Integer>() {
			@Override
			public Integer get() throws Throwable {
				return Integer.parseInt(response);
			}
		}, new ObjectSerialisationSupplier(response));
		int numberReceived = Integer.parseInt(response);
		assertEquals(numberEpxected, numberReceived, new ObjectSerialisationSupplier(response));
	}

	@Test
	void lMOpenAIDecide() {
		Utterances utterances = new Utterances();
		utterances.appendAssistantSays("What is your age?");
		utterances.appendUserSays("I am 50 years old.");
		boolean decision = LMOpenAI.decide(utterances,
				"Examine the following chat and decide if the user mentions their age.");
		assertTrue(decision);
	}

	@Test
	void lMOpenaiExtract() {
		int ageExpected = 50;

		Utterances utterances = new Utterances();
		utterances.appendAssistantSays("What is your age?");
		utterances.appendUserSays("I am " + ageExpected + " years old.");
		JsonElement extract = LMOpenAI.extract(utterances, "Scan the chat and find the user's age.");
		assertNotNull(extract);
		assertInstanceOf(JsonObject.class, extract);
		Set<Entry<String, JsonElement>> entrySet = ((JsonObject) extract).entrySet();
		assertEquals(1, entrySet.size(), new ObjectSerialisationSupplier(extract));
		Entry<String, JsonElement> entry = entrySet.iterator().next();
		assertDoesNotThrow(new ThrowingSupplier<Integer>() {
			@Override
			public Integer get() throws Throwable {
				return Integer.parseInt(entry.getValue().toString());
			}
		}, new ObjectSerialisationSupplier(extract));
		int ageExtracted = Integer.parseInt(entry.getValue().toString());
		assertEquals(ageExpected, ageExtracted, new ObjectSerialisationSupplier(extract));
	}

	@Test
	void lMOpenAISummarise() {
		Utterances utterances = new Utterances();
		utterances.appendAssistantSays(
				"Alright, let's get this over with. Tell me, where are you flying from? And, where do you plan to land your weary self? Lastly, on what exact date are you planning this journey?");
		utterances.appendUserSays("from Zurich");
		utterances.appendAssistantSays(
				"Alright, you're leaving from Zurich. Now, where do you think you're going? And don't forget, I still need that date.");
		utterances.appendUserSays("to London, Tomorrow");
		utterances.appendAssistantSays(
				"Alright, pick one and only one from this list: \"Bamboo Airways\", \"Pegasus Airlines\", or \"French Bee\". I don't have all day.");
		utterances.appendUserSays("Delta");
		utterances.appendAssistantSays(
				"Let me remind you again, Delta is NOT in the options. The options are \"Bamboo Airways\", \"Pegasus Airlines\", and \"French Bee\". Please select from THESE options, or I swear I'll cancel the whole transaction.\", or \"French Bee\". I don't have all day.");
		utterances.appendUserSays("bye");
		String summary = LMOpenAI.summarise(utterances,
				"Please summarise the following conversation. Be concise, but ensure that the key points and issues are included. Do not end the conversation abruptly. This is the conversation to summarize: ");
		assertTrue(summary.contains("conversation"));
	}
}
