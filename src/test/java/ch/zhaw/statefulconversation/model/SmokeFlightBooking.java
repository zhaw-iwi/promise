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
import com.google.gson.JsonPrimitive;

import ch.zhaw.statefulconversation.model.commons.states.DynamicGatherState;
import ch.zhaw.statefulconversation.model.commons.states.DynamicSingleChoiceState;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class SmokeFlightBooking {

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

    @BeforeAll
    static void setUp() {
        Storage storage = new Storage();
        SmokeFlightBooking.storageKeyFromSlots = "OrderSlots";
        storage.put(SmokeFlightBooking.storageKeyFromSlots,
                SmokeFlightBooking.GSON.toJsonTree(List.of("Departure", "Destination", "Date")));
        SmokeFlightBooking.departureExpected = "Zurich";
        SmokeFlightBooking.destinationExpected = "London";
        SmokeFlightBooking.dateExpected = "Tomorrow";
        SmokeFlightBooking.storageKeyToSlotValues = "OrderSlotsValues";
        SmokeFlightBooking.choiceExpected = "Pegasus Airlines";
        SmokeFlightBooking.storageKeyFromChoices = "Offers";
        storage.put(SmokeFlightBooking.storageKeyFromChoices,
                SmokeFlightBooking.GSON
                        .toJsonTree(List.of("Bamboo Airways", SmokeFlightBooking.choiceExpected, "French Bee")));
        SmokeFlightBooking.wrongChoice = "Delta";
        SmokeFlightBooking.storageKeyToChoice = "OffersChosen";

        State usersChooseOffer = new DynamicSingleChoiceState("UsersChooseOffer", new Final(),
                storage,
                SmokeFlightBooking.storageKeyFromChoices, SmokeFlightBooking.storageKeyToChoice);
        State usersSayFromToDate = new DynamicGatherState("UsersSayFromToDate", usersChooseOffer,
                storage, SmokeFlightBooking.storageKeyFromSlots,
                SmokeFlightBooking.storageKeyToSlotValues);
        SmokeFlightBooking.agent = new Agent("Flight Booking Assistant",
                "Flight booking assistant helping you book a flight.", usersSayFromToDate, storage);
    }

    @Test
    @Order(1)
    void start() {
        assertTrue(SmokeFlightBooking.agent.isActive());
        String response = SmokeFlightBooking.agent.start();
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertTrue(SmokeFlightBooking.agent.isActive());
    }

    @Test
    @Order(2)
    void provideFrom() {
        String response = SmokeFlightBooking.agent.respond("from " + SmokeFlightBooking.departureExpected);
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertTrue(SmokeFlightBooking.agent.isActive());
    }

    @Test
    @Order(3)
    void provideToDate() {
        String response = SmokeFlightBooking.agent
                .respond("to " + SmokeFlightBooking.destinationExpected + ", " + SmokeFlightBooking.dateExpected);
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertTrue(SmokeFlightBooking.agent.isActive());
    }

    @Test
    @Order(4)
    void slotValuesStored() {
        assertTrue(SmokeFlightBooking.agent.storage().containsKey(SmokeFlightBooking.storageKeyToSlotValues));
        JsonElement extract = SmokeFlightBooking.agent.storage().get(SmokeFlightBooking.storageKeyToSlotValues);
        assertInstanceOf(JsonObject.class, extract);
        Set<Entry<String, JsonElement>> entrySet = ((JsonObject) extract).entrySet();
        assertEquals(3, entrySet.size(), new ObjectSerialisationSupplier(extract));

        Set<String> slotValuesProvided = new HashSet<String>();
        for (Entry<String, JsonElement> current : entrySet) {
            slotValuesProvided.add(current.getValue().getAsString());
        }

        assertTrue(slotValuesProvided.contains(SmokeFlightBooking.departureExpected));
        assertTrue(slotValuesProvided.contains(SmokeFlightBooking.destinationExpected));
        assertTrue(slotValuesProvided.contains(SmokeFlightBooking.dateExpected));
    }

    @Test
    @Order(5)
    void wrongChoice() {
        String response = SmokeFlightBooking.agent
                .respond(SmokeFlightBooking.wrongChoice);
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertTrue(SmokeFlightBooking.agent.isActive());
    }

    @Test
    @Order(6)
    void makeChoice() {
        String response = SmokeFlightBooking.agent
                .respond(SmokeFlightBooking.choiceExpected);
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertFalse(SmokeFlightBooking.agent.isActive());
    }

    @Test
    @Order(7)
    void choiceStored() {
        assertTrue(SmokeFlightBooking.agent.storage().containsKey(SmokeFlightBooking.storageKeyToChoice));
        String choiceMade;
        JsonElement jsonElement = SmokeFlightBooking.agent.storage().get(SmokeFlightBooking.storageKeyToChoice);

        if (jsonElement instanceof JsonObject object) {
            Set<Entry<String, JsonElement>> entrySet = object.entrySet();
            assertEquals(1, entrySet.size(), new ObjectSerialisationSupplier(jsonElement));
            choiceMade = entrySet.iterator().next().getValue().getAsString();
        } else if (jsonElement instanceof JsonPrimitive) {
            choiceMade = jsonElement.toString().replaceAll("\"", "");
        } else {
            throw new RuntimeException("expected either JsonObject or JsonPrimitive but was " + jsonElement.getClass());
        }
        assertEquals(SmokeFlightBooking.choiceExpected, choiceMade, new ObjectSerialisationSupplier(choiceMade));
    }
}
