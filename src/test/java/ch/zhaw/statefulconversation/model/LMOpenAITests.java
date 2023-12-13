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
		assertInstanceOf(String.class, OpenAIProperties.instance().getModel());
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
}
