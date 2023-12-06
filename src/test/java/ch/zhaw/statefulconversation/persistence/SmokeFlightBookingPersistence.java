package ch.zhaw.statefulconversation.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.ObjectSerialisationSupplier;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.commons.states.DynamicGatherState;
import ch.zhaw.statefulconversation.model.commons.states.DynamicSingleChoiceState;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
public class SmokeFlightBookingPersistence {

    private static final Gson GSON = new Gson();

    private static String departureExpected;
    private static String destinationExpected;
    private static String dateExpected;
    private static String storageKeyFromSlots;
    private static String storageKeyToSlotValues;
    private static String choiceExpected;
    private static String wrongChoice;
    private static String storageKeyFromChoices;
    private static String storageKeyToChoice;
    private static Agent agent;
    private static UUID agentID;

    @BeforeAll
    private static void setUp() {
        Storage storage = new Storage();
        SmokeFlightBookingPersistence.storageKeyFromSlots = "OrderSlots";
        storage.put(SmokeFlightBookingPersistence.storageKeyFromSlots,
                SmokeFlightBookingPersistence.GSON.toJsonTree(List.of("Departure", "Destination", "Date")));
        SmokeFlightBookingPersistence.departureExpected = "Zurich";
        SmokeFlightBookingPersistence.destinationExpected = "London";
        SmokeFlightBookingPersistence.dateExpected = "Tomorrow";
        SmokeFlightBookingPersistence.storageKeyToSlotValues = "OrderSlotsValues";
        SmokeFlightBookingPersistence.choiceExpected = "Pegasus Airlines";
        SmokeFlightBookingPersistence.storageKeyFromChoices = "Offers";
        storage.put(SmokeFlightBookingPersistence.storageKeyFromChoices,
                SmokeFlightBookingPersistence.GSON
                        .toJsonTree(
                                List.of("Bamboo Airways", SmokeFlightBookingPersistence.choiceExpected, "French Bee")));
        SmokeFlightBookingPersistence.wrongChoice = "Delta";
        SmokeFlightBookingPersistence.storageKeyToChoice = "OffersChosen";

        State usersChooseOffer = new DynamicSingleChoiceState("UsersChooseOffer", new Final(),
                storage,
                SmokeFlightBookingPersistence.storageKeyFromChoices, SmokeFlightBookingPersistence.storageKeyToChoice);
        State usersSayFromToDate = new DynamicGatherState("UsersSayFromToDate", usersChooseOffer,
                storage, SmokeFlightBookingPersistence.storageKeyFromSlots,
                SmokeFlightBookingPersistence.storageKeyToSlotValues);
        SmokeFlightBookingPersistence.agent = new Agent("Flight Booking Assistant",
                "Flight booking assistant helping you book a flight.", usersSayFromToDate, storage);
    }

    @Autowired
    private AgentRepository agentRepository;

    @Test
    @Order(1)
    void testSave() {
        Agent agent = this.agentRepository.save(SmokeFlightBookingPersistence.agent);
        SmokeFlightBookingPersistence.agentID = agent.getId();
        assertNotNull(SmokeFlightBookingPersistence.agentID);
    }

    @Test
    @Order(2)
    void testStart() {
        Optional<Agent> maybeAgent = this.agentRepository.findById(SmokeFlightBookingPersistence.agentID);
        assertTrue(maybeAgent.isPresent());
        Agent agent = maybeAgent.get();

        String starter = agent.start();
        assertNotNull(starter);
        assertFalse(starter.isEmpty());

        this.agentRepository.save(agent);
    }

    @Test
    @Order(3)
    void testProvideFrom() {
        Optional<Agent> maybeAgent = this.agentRepository.findById(SmokeFlightBookingPersistence.agentID);
        assertTrue(maybeAgent.isPresent());
        Agent agent = maybeAgent.get();

        String response = agent.respond("from " + SmokeFlightBookingPersistence.departureExpected);
        assertNotNull(response);
        assertFalse(response.isEmpty());

        this.agentRepository.save(agent);
    }

    @Test
    @Order(4)
    void testProvideToDate() {
        Optional<Agent> maybeAgent = this.agentRepository.findById(SmokeFlightBookingPersistence.agentID);
        assertTrue(maybeAgent.isPresent());
        Agent agent = maybeAgent.get();

        String response = agent
                .respond("to " + SmokeFlightBookingPersistence.destinationExpected + ", "
                        + SmokeFlightBookingPersistence.dateExpected);
        assertNotNull(response);
        assertFalse(response.isEmpty());

        this.agentRepository.save(agent);
    }

    @Test
    @Order(5)
    void testSlotValuesStored() {
        Optional<Agent> maybeAgent = this.agentRepository.findById(SmokeFlightBookingPersistence.agentID);
        assertTrue(maybeAgent.isPresent());
        Agent agent = maybeAgent.get();

        assertTrue(agent.storage().containsKey(SmokeFlightBookingPersistence.storageKeyToSlotValues));
        JsonElement extract = agent.storage().get(SmokeFlightBookingPersistence.storageKeyToSlotValues);
        assertInstanceOf(JsonObject.class, extract);
        Set<Entry<String, JsonElement>> entrySet = ((JsonObject) extract).entrySet();
        assertEquals(3, entrySet.size(), new ObjectSerialisationSupplier(extract));

        Set<String> slotValuesProvided = new HashSet<String>();
        for (Entry<String, JsonElement> current : entrySet) {
            slotValuesProvided.add(current.getValue().getAsString());
        }

        assertTrue(slotValuesProvided.contains(SmokeFlightBookingPersistence.departureExpected));
        assertTrue(slotValuesProvided.contains(SmokeFlightBookingPersistence.destinationExpected));
        assertTrue(slotValuesProvided.contains(SmokeFlightBookingPersistence.dateExpected));
    }

    @Test
    @Order(6)
    void testWrongChoice() {
        Optional<Agent> maybeAgent = this.agentRepository.findById(SmokeFlightBookingPersistence.agentID);
        assertTrue(maybeAgent.isPresent());
        Agent agent = maybeAgent.get();

        String response = agent
                .respond(SmokeFlightBookingPersistence.wrongChoice);
        assertNotNull(response);
        assertFalse(response.isEmpty());

        this.agentRepository.save(agent);
    }

    @Test
    @Order(7)
    void testMakeChoice() {
        Optional<Agent> maybeAgent = this.agentRepository.findById(SmokeFlightBookingPersistence.agentID);
        assertTrue(maybeAgent.isPresent());
        Agent agent = maybeAgent.get();

        String response = agent
                .respond(SmokeFlightBookingPersistence.choiceExpected);
        assertNull(response, new ObjectSerialisationSupplier(response));

        this.agentRepository.save(agent);
    }

    @Test
    @Order(8)
    void testChoiceStored() {
        Optional<Agent> maybeAgent = this.agentRepository.findById(SmokeFlightBookingPersistence.agentID);
        assertTrue(maybeAgent.isPresent());
        Agent agent = maybeAgent.get();

        assertTrue(agent.storage().containsKey(SmokeFlightBookingPersistence.storageKeyToChoice));
        String choiceMade;
        JsonElement jsonElement = agent.storage().get(SmokeFlightBookingPersistence.storageKeyToChoice);

        if (jsonElement instanceof JsonObject) {
            Set<Entry<String, JsonElement>> entrySet = ((JsonObject) jsonElement).entrySet();
            assertEquals(1, entrySet.size(), new ObjectSerialisationSupplier(jsonElement));
            choiceMade = entrySet.iterator().next().getValue().getAsString();
        } else if (jsonElement instanceof JsonPrimitive) {
            choiceMade = jsonElement.toString().replaceAll("\"", "");
        } else {
            throw new RuntimeException("expected either JsonObject or JsonPrimitive but was " + jsonElement.getClass());
        }
        assertEquals(SmokeFlightBookingPersistence.choiceExpected, choiceMade,
                new ObjectSerialisationSupplier(choiceMade));
    }
}
