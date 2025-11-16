package ch.zhaw.statefulconversation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.model.commons.states.DynamicSingleChoiceStateShrinking;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class DynamicSingleChoiceStateShrinkingTest {

    private static final Gson GSON = new Gson();

    private static Storage storage;
    private static String storageKeyTo;
    private static State state;
    private static String storageKeyFrom;
    private static List<String> availableChoices;
    private static String wrongReason;
    private static String reasonExpected;
    private static JsonPrimitive choiceExpected;

    @BeforeAll
    static void setUp() {
        DynamicSingleChoiceStateShrinkingTest.storage = new Storage();
        DynamicSingleChoiceStateShrinkingTest.storageKeyTo = "choice";
        DynamicSingleChoiceStateShrinkingTest.storageKeyFrom = "available_choices";
        DynamicSingleChoiceStateShrinkingTest.availableChoices = List.of("Müdigkeit bei der Arbeit.",
                "Sorgen wegen Boosterimpfung");
        DynamicSingleChoiceStateShrinkingTest.choiceExpected = new JsonPrimitive("Müdigkeit bei der Arbeit.");
        DynamicSingleChoiceStateShrinkingTest.storage.put(DynamicSingleChoiceStateShrinkingTest.storageKeyFrom,
                DynamicSingleChoiceStateShrinkingTest.GSON
                        .toJsonTree(DynamicSingleChoiceStateShrinkingTest.availableChoices));
        DynamicSingleChoiceStateShrinkingTest.wrongReason = "Hat es noch weitere Themen für mich?";
        DynamicSingleChoiceStateShrinkingTest.reasonExpected = "Ich möchte über die Müdigkeit sprechen.";

        State innerState = new DynamicSingleChoiceStateShrinking("chooseOption", new Final(),
                DynamicSingleChoiceStateShrinkingTest.storage, DynamicSingleChoiceStateShrinkingTest.storageKeyFrom,
                DynamicSingleChoiceStateShrinkingTest.storageKeyTo);
        Decision trigger = new StaticDecision("review the chat and determine if the user wants to exit.");
        Transition transition = new Transition(List.of(trigger), List.of(), new Final());
        DynamicSingleChoiceStateShrinkingTest.state = new OuterState(
                "Du bist ein Psychologe mit Fachwissen im Umgang mit gehörlosen Jugendlichen. Du bist vertraut mit den alltäglichen Herausforderungen, denen gehörbeeinträchtigte Menschen gegenüberstehen, und verstehst, wie diese sich auf ihre sozialen Beziehungen auswirken können..",
                "medicalSupport", List.of(transition), innerState);
    }

    @Test
    @Order(1)
    void start() {
        Response response = DynamicSingleChoiceStateShrinkingTest.state.start();
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());
    }

    @Test
    @Order(2)
    void provideWrongAnswer() {
        Response response = null;
        try {
            response = DynamicSingleChoiceStateShrinkingTest.state
                    .respond(DynamicSingleChoiceStateShrinkingTest.wrongReason);
        } catch (TransitionException e) {
            assertTrue(false);
        }
        assertNotNull(response);
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());
    }

    @Test
    @Order(3)
    void completeSlotValues() {
        Response response = null;
        try {
            response = DynamicSingleChoiceStateShrinkingTest.state
                    .respond(DynamicSingleChoiceStateShrinkingTest.reasonExpected);
        } catch (TransitionException e) {
            assertTrue(false);
        }
        assertNotNull(response);
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());
        assertFalse(DynamicSingleChoiceStateShrinkingTest.state.isActive());
    }

    @Test
    @Order(4)
    void slotValuesStored() {
        assertTrue(DynamicSingleChoiceStateShrinkingTest.storage
                .containsKey(DynamicSingleChoiceStateShrinkingTest.storageKeyTo));
        assertInstanceOf(JsonPrimitive.class,
                DynamicSingleChoiceStateShrinkingTest.storage.get(DynamicSingleChoiceStateShrinkingTest.storageKeyTo));
        JsonPrimitive extract = (JsonPrimitive) DynamicSingleChoiceStateShrinkingTest.storage
                .get(DynamicSingleChoiceStateShrinkingTest.storageKeyTo);
        assertInstanceOf(JsonPrimitive.class, extract);
        JsonElement extractArray = DynamicSingleChoiceStateShrinkingTest.storage.get(storageKeyFrom);
        assertEquals(1, extractArray.getAsJsonArray().size());

        assertEquals(extract.getAsString(), DynamicSingleChoiceStateShrinkingTest.choiceExpected.getAsString());
    }
}
