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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.model.commons.states.GatherState;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
public class GatherStateTest {

    private static Storage storage;
    private static String storageKeyTo;
    private static State state;
    private static List<String> slots;
    private static String departureExpected;
    private static String destinationExpected;
    private static String dateExpected;

    @BeforeAll
    private static void setUp() {
        GatherStateTest.storage = new Storage();
        GatherStateTest.storageKeyTo = "slotValues";
        GatherStateTest.slots = List.of("Departure", "Destination", "Date");
        GatherStateTest.departureExpected = "Zurich";
        GatherStateTest.destinationExpected = "London";
        GatherStateTest.dateExpected = "Tomorrow";

        State innerState = new GatherState("askFromToDate", GatherStateTest.slots,
                new Final(), GatherStateTest.storage, GatherStateTest.storageKeyTo);
        Decision trigger = new StaticDecision("review the chat and determine if the user wants to exit.");
        Transition transition = new Transition(List.of(trigger), List.of(), new Final());
        GatherStateTest.state = new OuterState("you are a grumpy flight booking assistant.", "grumpyAssistant",
                List.of(transition), innerState);
    }

    @Test
    @Order(1)
    void testStart() {
        String response = GatherStateTest.state.start();
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(2)
    void testProvideOneSlotValue() {
        String response = null;
        try {
            response = GatherStateTest.state.respond("from " + GatherStateTest.departureExpected);
        } catch (TransitionException e) {
            assertTrue(false);
        }
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(3)
    void testCompleteSlotValues() {

        TransitionException e = assertThrows(TransitionException.class, () -> {
            GatherStateTest.state
                    .respond("to " + GatherStateTest.destinationExpected + ", " + GatherStateTest.dateExpected);
        });
        assertNotNull(e.getSubsequentState());
        assertInstanceOf(Final.class, e.getSubsequentState());
    }

    @Test
    @Order(4)
    void testSlotValuesStored() {
        assertTrue(GatherStateTest.storage.containsKey(GatherStateTest.storageKeyTo));
        assertInstanceOf(JsonObject.class, GatherStateTest.storage.get(GatherStateTest.storageKeyTo));
        JsonObject extract = (JsonObject) GatherStateTest.storage.get(GatherStateTest.storageKeyTo);
        Set<Entry<String, JsonElement>> entrySet = extract.entrySet();
        assertEquals(3, entrySet.size(), new ObjectSerialisationSupplier(extract));

        Set<String> slotValuesProvided = new HashSet<String>();
        for (Entry<String, JsonElement> current : entrySet) {
            slotValuesProvided.add(current.getValue().getAsString());
        }

        assertTrue(slotValuesProvided.contains(GatherStateTest.departureExpected));
        assertTrue(slotValuesProvided.contains(GatherStateTest.destinationExpected));
        assertTrue(slotValuesProvided.contains(GatherStateTest.dateExpected));
    }
}
