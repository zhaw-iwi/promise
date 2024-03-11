package ch.zhaw.statefulconversation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import com.google.gson.JsonPrimitive;

import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.model.commons.states.DynamicActionAgreementState;
import ch.zhaw.statefulconversation.model.commons.states.DynamicCauseAssessmentState;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
public class SmokeMedicalAssistant {

    private static final Gson GSON = new Gson();

    private static String storageKeyToReason;
    private static String storageKeyFromMissingAgreement;
    private static List<String> agreements;
    private static String wrongReason;
    private static String reasonExpected;
    private static String keyAvailableActions;
    private static List<String> actions;
    private static String agreementExpected;
    private static String storageKeyToAction;
    private static Agent agent;
    private static String askAboutOption;

    @BeforeAll
    private static void setUp() {
        Storage storage = new Storage();
        SmokeMedicalAssistant.storageKeyToReason = "reason";
        SmokeMedicalAssistant.storageKeyFromMissingAgreement = "missedAgreement";
        SmokeMedicalAssistant.agreements = List.of("Patient ist nicht schwimmen gegangen.");
        storage.put(SmokeMedicalAssistant.storageKeyFromMissingAgreement,
                SmokeMedicalAssistant.GSON.toJsonTree(SmokeMedicalAssistant.agreements));
        SmokeMedicalAssistant.wrongReason = "Muss ich das sagen?";
        SmokeMedicalAssistant.reasonExpected = "Ich fühle mich nicht so wohl mit vielen Leuten um mich herum.";
        SmokeMedicalAssistant.askAboutOption = "Weshalb empfiehlst du diese Massnahme?";

        SmokeMedicalAssistant.keyAvailableActions = "availableActions";
        SmokeMedicalAssistant.storageKeyToAction = "chosenAction";
        SmokeMedicalAssistant.agreements = List.of("Patient ist nicht schwimmen gegangen.");
        SmokeMedicalAssistant.actions = List.of("Aquafit in der Gruppe", "Schwimmen in offenenem Gewässer");
        storage.put(SmokeMedicalAssistant.keyAvailableActions,
                SmokeMedicalAssistant.GSON.toJsonTree(SmokeMedicalAssistant.actions));
        SmokeMedicalAssistant.agreementExpected = "Schwimmen in offenem Gewässer";

        State usersAgreeOnAction = new DynamicActionAgreementState("UsersAgreenOnAction", new Final(),
                storage,
                SmokeMedicalAssistant.storageKeyFromMissingAgreement, SmokeMedicalAssistant.storageKeyToReason,
                SmokeMedicalAssistant.keyAvailableActions, SmokeMedicalAssistant.storageKeyToAction);
        State usersSayReason = new DynamicCauseAssessmentState("UsersSayReason", usersAgreeOnAction,
                storage, SmokeMedicalAssistant.storageKeyFromMissingAgreement,
                SmokeMedicalAssistant.storageKeyToReason);
        Decision trigger = new StaticDecision("review the chat and determine if the user wants to exit.");
        Transition transition = new Transition(List.of(trigger), List.of(), new Final());
        OuterState medicalAssistant = new OuterState(
                "Du bist ein erfahrener Arzt mit Spezialisierung auf chronische Erkrankungen. Du bist einfühlsam und verständnisvoll. Versuche, so kurz wie möglich zu antworten, aber stelle sicher, dass deine Antworten hilfreich und unterstützend sind.",
                "medicalSupport",
                List.of(transition), usersSayReason);
        SmokeMedicalAssistant.agent = new Agent("Medical Assistant", "Providing medical assistance.", medicalAssistant,
                storage);
    }

    @Test
    @Order(1)
    void testStart() {
        String response = SmokeMedicalAssistant.agent.start();
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(2)
    void testProvideWrongAnswer() {
        String response = null;
        response = SmokeMedicalAssistant.agent
                .respond(SmokeMedicalAssistant.wrongReason);
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(3)
    void testProvideReasonExpected() {

        String response = SmokeMedicalAssistant.agent
                .respond(SmokeMedicalAssistant.reasonExpected);
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(4)
    void testSlotValuesStored() {
        assertTrue(SmokeMedicalAssistant.agent.storage().containsKey(SmokeMedicalAssistant.storageKeyToReason));
        JsonElement extract = SmokeMedicalAssistant.agent.storage().get(SmokeMedicalAssistant.storageKeyToReason);
        assertInstanceOf(JsonObject.class, extract);
        Set<Entry<String, JsonElement>> entrySet = ((JsonObject) extract).entrySet();
        assertEquals(1, entrySet.size(), new ObjectSerialisationSupplier(extract));
        Set<String> slotValuesProvided = new HashSet<String>();
        for (Entry<String, JsonElement> current : entrySet) {
            slotValuesProvided.add(current.getValue().getAsString());
        }

        assertTrue(slotValuesProvided.contains(SmokeMedicalAssistant.reasonExpected));
    }

    @Test
    @Order(5)
    void testAskAboutOption() {
        String response = SmokeMedicalAssistant.agent
                .respond(SmokeMedicalAssistant.askAboutOption);
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(6)
    void testAcceptAction() {
        String response = SmokeMedicalAssistant.agent
                .respond(SmokeMedicalAssistant.agreementExpected);
        assertNull(response, new ObjectSerialisationSupplier(response));
    }

    @Test
    @Order(7)
    void testAgreementStored() {
        assertTrue(SmokeMedicalAssistant.agent.storage().containsKey(SmokeMedicalAssistant.storageKeyToAction));
        String choiceMade;
        JsonElement jsonElement = SmokeMedicalAssistant.agent.storage().get(SmokeMedicalAssistant.storageKeyToAction);

        if (jsonElement instanceof JsonObject) {
            Set<Entry<String, JsonElement>> entrySet = ((JsonObject) jsonElement).entrySet();
            assertEquals(1, entrySet.size(), new ObjectSerialisationSupplier(jsonElement));
            choiceMade = entrySet.iterator().next().getValue().getAsString();
        } else if (jsonElement instanceof JsonPrimitive) {
            choiceMade = jsonElement.toString().replaceAll("\"", "");
        } else {
            throw new RuntimeException("expected either JsonObject or JsonPrimitive but was " + jsonElement.getClass());
        }
        assertEquals(SmokeMedicalAssistant.agreementExpected, choiceMade, new ObjectSerialisationSupplier(choiceMade));
    }
}