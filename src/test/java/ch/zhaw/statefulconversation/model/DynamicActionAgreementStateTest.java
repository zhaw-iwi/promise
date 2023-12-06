package ch.zhaw.statefulconversation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import ch.zhaw.statefulconversation.model.commons.states.DynamicActionAgreementState;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
public class DynamicActionAgreementStateTest {

    private static final Gson GSON = new Gson();

    private static Storage storage;
    private static String keyFindReason;
    private static String keyReason;
    private static String keyAvailableActions;
    private static String storageKeyTo;
    private static State state;
    private static List<String> agreements;
    private static List<String> actions;
    private static String agreementExpected;

    @BeforeAll
    private static void setUp() {
        DynamicActionAgreementStateTest.storage = new Storage();
        DynamicActionAgreementStateTest.keyFindReason = "findReason";
        DynamicActionAgreementStateTest.keyReason = "reason";
        DynamicActionAgreementStateTest.keyAvailableActions = "availableActions";
        DynamicActionAgreementStateTest.storageKeyTo = "chosenAction";
        DynamicActionAgreementStateTest.agreements = List.of("Patient ist nicht schwimmen gegangen.");
        DynamicActionAgreementStateTest.actions = List.of("Aquafit in der Gruppe", "Schwimmen in offenenem Gewässer");
        DynamicActionAgreementStateTest.storage.put(DynamicActionAgreementStateTest.keyFindReason,
                DynamicActionAgreementStateTest.GSON.toJsonTree(DynamicActionAgreementStateTest.agreements));
        DynamicActionAgreementStateTest.storage.put(DynamicActionAgreementStateTest.keyReason,
                DynamicActionAgreementStateTest.GSON.toJsonTree(List.of("Der Patient ist nicht gerne unter Menschen")));
        DynamicActionAgreementStateTest.storage.put(DynamicActionAgreementStateTest.keyAvailableActions,
                DynamicActionAgreementStateTest.GSON.toJsonTree(DynamicActionAgreementStateTest.actions));
        DynamicActionAgreementStateTest.agreementExpected = "Aquafit in der Gruppe";

        State innerState = new DynamicActionAgreementState("agreeOnAction",
                new Final(), DynamicActionAgreementStateTest.storage, DynamicActionAgreementStateTest.keyFindReason,
                DynamicActionAgreementStateTest.keyReason, DynamicActionAgreementStateTest.keyAvailableActions,
                DynamicActionAgreementStateTest.storageKeyTo);
        Decision trigger = new StaticDecision("review the chat and determine if the user wants to exit.");
        Transition transition = new Transition(List.of(trigger), List.of(), new Final());
        DynamicActionAgreementStateTest.state = new OuterState(
                "Du bist Arzt und spezialisiert auf chronische Erkrankungen. Halte dich immer so kurz wie möglich.",
                "medicalSupport",
                List.of(transition), innerState);
    }

    @Test
    @Order(1)
    void testStart() {
        String response = DynamicActionAgreementStateTest.state.start();
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(2)
    void testAskQuestionAboutAction() {
        String response = null;
        try {
            response = DynamicActionAgreementStateTest.state.respond("Sind beide Varianten gleich gut?");
        } catch (TransitionException e) {
            assertTrue(false);
        }
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(3)
    void testAgreeOnAction() {

        TransitionException e = assertThrows(TransitionException.class, () -> {
            DynamicActionAgreementStateTest.state
                    .respond("Ich möchte zuerst das " +
                            DynamicActionAgreementStateTest.agreementExpected
                            + ", probieren.");
        });
        assertNotNull(e.getSubsequentState());
        assertInstanceOf(Final.class, e.getSubsequentState());
    }

    @Test
    @Order(4)
    void testSlotValuesStored() {
        assertTrue(DynamicActionAgreementStateTest.storage.containsKey(DynamicActionAgreementStateTest.storageKeyTo));
        assertInstanceOf(JsonObject.class,
                DynamicActionAgreementStateTest.storage.get(DynamicActionAgreementStateTest.storageKeyTo));
        JsonObject extract = (JsonObject) DynamicActionAgreementStateTest.storage
                .get(DynamicActionAgreementStateTest.storageKeyTo);
        assertInstanceOf(JsonObject.class, extract);
        Set<Entry<String, JsonElement>> entrySet = extract.entrySet();
        assertEquals(1, entrySet.size(), new ObjectSerialisationSupplier(extract));

        Set<String> slotValuesProvided = new HashSet<String>();
        for (Entry<String, JsonElement> current : entrySet) {
            slotValuesProvided.add(current.getValue().getAsString());
        }

        assertTrue(slotValuesProvided.contains(DynamicActionAgreementStateTest.agreementExpected));
    }
}
