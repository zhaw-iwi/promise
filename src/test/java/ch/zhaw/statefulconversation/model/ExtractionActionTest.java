package ch.zhaw.statefulconversation.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.zhaw.statefulconversation.model.commons.actions.StaticExtractionAction;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class ExtractionActionTest {

    private static Storage storage;
    private static String storageKeyTo;
    private static State state;
    private static String userName;

    @BeforeAll
    static void setUp() {
        ExtractionActionTest.storage = new Storage();
        ExtractionActionTest.storageKeyTo = "name";
        ExtractionActionTest.userName = "Mike";
        Decision trigger = new StaticDecision(
                "Examine the following chat and decide if the user mentions their name.");
        Action action = new StaticExtractionAction("Analyse the following conversation and extract the person's name.",
                storage, ExtractionActionTest.storageKeyTo);
        Transition transition = new Transition(List.of(trigger), List.of(action), new Final());
        ExtractionActionTest.state = new State("You are a grumpy assistant.", "greeting",
                "Say hello and ask for their name.",
                List.of(transition));
    }

    @Test
    @Order(1)
    void start() {
        Response response = ExtractionActionTest.state.start();
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());
    }

    @Test
    @Order(2)
    void respond() {
        Response response = null;
        try {
            response = ExtractionActionTest.state.respond("my name is useless.");
        } catch (TransitionException e) {
            assertTrue(false);
        }
        assertNotNull(response);
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());
    }

    @Test
    @Order(3)
    void transition() {
        TransitionException e = assertThrows(TransitionException.class, () -> {
            ExtractionActionTest.state.respond("my name is " + ExtractionActionTest.userName + ".");
        });
        assertNotNull(e.getSubsequentState());
        assertInstanceOf(Final.class, e.getSubsequentState());
    }

    @Test
    @Order(4)
    void extractStored() {
        assertTrue(ExtractionActionTest.storage.containsKey(ExtractionActionTest.storageKeyTo));
        assertInstanceOf(JsonObject.class, ExtractionActionTest.storage.get(ExtractionActionTest.storageKeyTo));
        JsonObject extract = (JsonObject) ExtractionActionTest.storage.get(ExtractionActionTest.storageKeyTo);
        Set<Entry<String, JsonElement>> entrySet = extract.entrySet();
        assertEquals(1, entrySet.size(), new ObjectSerialisationSupplier(extract));
        Entry<String, JsonElement> entry = entrySet.iterator().next();
        assertDoesNotThrow(new ThrowingSupplier<String>() {
            @Override
            public String get() throws Throwable {
                return entry.getValue().getAsString();
            }
        }, new ObjectSerialisationSupplier(extract));
        String nameExtracted = entry.getValue().getAsString();
        assertEquals(ExtractionActionTest.userName, nameExtracted, new ObjectSerialisationSupplier(extract));
    }
}
