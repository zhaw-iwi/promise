package ch.zhaw.statefulconversation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.model.commons.states.SingleChoiceState;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class SingleChoiceStateTest {

    private static Storage storage;
    private static String storageKeyTo;
    private static State state;
    private static List<String> choices;
    private static String choiceExpected;
    private static String wrongChoice;

    @BeforeAll
    static void setUp() {
        SingleChoiceStateTest.storage = new Storage();
        SingleChoiceStateTest.storageKeyTo = "choice";
        SingleChoiceStateTest.choiceExpected = "Pegasus Airlines";
        SingleChoiceStateTest.choices = List.of("Bamboo Airways", SingleChoiceStateTest.choiceExpected, "French Bee");
        SingleChoiceStateTest.wrongChoice = "Delta";

        State innerState = new SingleChoiceState("letChoose", SingleChoiceStateTest.choices,
                new Final(), SingleChoiceStateTest.storage, SingleChoiceStateTest.storageKeyTo);
        Decision trigger = new StaticDecision("Review the chat and determine if the user wants to exit.");
        Transition transition = new Transition(List.of(trigger), List.of(), new Final());
        SingleChoiceStateTest.state = new OuterState("You are a grumpy flight booking assistant.", "grumpyAssistant",
                List.of(transition), innerState);
    }

    @Test
    @Order(1)
    void start() {
        String response = SingleChoiceStateTest.state.start();
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(2)
    void wrongChoice() {
        String response = null;
        try {
            response = SingleChoiceStateTest.state.respond(SingleChoiceStateTest.wrongChoice);
        } catch (TransitionException e) {
            assertTrue(false);
        }
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(3)
    void makeChoice() {
        String response = null;
        try {
            response = SingleChoiceStateTest.state
                    .respond(SingleChoiceStateTest.choiceExpected);
        } catch (TransitionException e) {
            assertTrue(false);
        }

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertFalse(SingleChoiceStateTest.state.isActive());
    }

    @Test
    @Order(4)
    void choiceStored() {
        assertTrue(SingleChoiceStateTest.storage.containsKey(SingleChoiceStateTest.storageKeyTo));
        assertInstanceOf(JsonObject.class, SingleChoiceStateTest.storage.get(SingleChoiceStateTest.storageKeyTo));
        JsonObject extract = (JsonObject) SingleChoiceStateTest.storage.get(SingleChoiceStateTest.storageKeyTo);
        Set<Entry<String, JsonElement>> entrySet = extract.entrySet();
        assertEquals(1, entrySet.size(), new ObjectSerialisationSupplier(extract));

        String choiceMade = entrySet.iterator().next().getValue().getAsString();
        assertEquals(SingleChoiceStateTest.choiceExpected, choiceMade, new ObjectSerialisationSupplier(choiceMade));
    }

}
