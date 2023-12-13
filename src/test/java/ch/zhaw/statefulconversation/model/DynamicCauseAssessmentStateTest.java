package ch.zhaw.statefulconversation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.model.commons.states.DynamicCauseAssessmentState;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class DynamicCauseAssessmentStateTest {

    private static final Gson GSON = new Gson();

    private static Storage storage;
    private static String storageKeyTo;
    private static State state;
    private static String storageKeyFrom;
    private static String missedAgreement;
    private static String wrongReason;
    private static String reasonExpected;

    @BeforeAll
    static void setUp() {
        DynamicCauseAssessmentStateTest.storage = new Storage();
        DynamicCauseAssessmentStateTest.storageKeyTo = "reason";
        DynamicCauseAssessmentStateTest.storageKeyFrom = "missed_agreement";
        DynamicCauseAssessmentStateTest.missedAgreement = "Patient ist nicht schwimmen gegangen.";
        DynamicCauseAssessmentStateTest.storage.put(DynamicCauseAssessmentStateTest.storageKeyFrom,
                DynamicCauseAssessmentStateTest.GSON.toJsonTree(DynamicCauseAssessmentStateTest.missedAgreement));
        DynamicCauseAssessmentStateTest.wrongReason = "Muss ich das sagen?";
        DynamicCauseAssessmentStateTest.reasonExpected = "Ich fühle mich nicht so wohl mit vielen Leuten um mich herum.";

        State innerState = new DynamicCauseAssessmentState("find_reason", new Final(),
                DynamicCauseAssessmentStateTest.storage, DynamicCauseAssessmentStateTest.storageKeyFrom,
                DynamicCauseAssessmentStateTest.storageKeyTo);
        Decision trigger = new StaticDecision("review the chat and determine if the user wants to exit.");
        Transition transition = new Transition(List.of(trigger), List.of(), new Final());
        DynamicCauseAssessmentStateTest.state = new OuterState(
                "Du bist Arzt und spezialisiert auf chronische Erkrankungen. Halte dich immer so kurz wie möglich.",
                "medicalSupport", List.of(transition), innerState);

    }

    @Test
    @Order(1)
    void start() {
        String response = DynamicCauseAssessmentStateTest.state.start();
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(2)
    void provideWrongAnswer() {
        String response = null;
        try {
            response = DynamicCauseAssessmentStateTest.state
                    .respond(DynamicCauseAssessmentStateTest.wrongReason);
        } catch (TransitionException e) {
            assertTrue(false);
        }
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(3)
    void completeSlotValues() {
        String response = null;
        try {
            response = DynamicCauseAssessmentStateTest.state
                    .respond(DynamicCauseAssessmentStateTest.reasonExpected);
        } catch (TransitionException e) {
            assertTrue(false);
        }

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertFalse(DynamicCauseAssessmentStateTest.state.isActive());
    }

    @Test
    @Order(4)
    void slotValuesStored() {
        assertTrue(DynamicCauseAssessmentStateTest.storage.containsKey(DynamicCauseAssessmentStateTest.storageKeyTo));
        assertInstanceOf(JsonObject.class,
                DynamicCauseAssessmentStateTest.storage.get(DynamicCauseAssessmentStateTest.storageKeyTo));
        JsonObject extract = (JsonObject) DynamicCauseAssessmentStateTest.storage
                .get(DynamicCauseAssessmentStateTest.storageKeyTo);
        assertInstanceOf(JsonObject.class, extract);
        Set<Entry<String, JsonElement>> entrySet = extract.entrySet();
        assertEquals(1, entrySet.size(), new ObjectSerialisationSupplier(extract));
        Set<String> slotValuesProvided = new HashSet<String>();
        for (Entry<String, JsonElement> current : entrySet) {
            slotValuesProvided.add(current.getValue().getAsString());
        }

        assertTrue(slotValuesProvided.contains(DynamicCauseAssessmentStateTest.reasonExpected));
    }
}
