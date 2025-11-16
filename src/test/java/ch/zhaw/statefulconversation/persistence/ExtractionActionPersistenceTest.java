package ch.zhaw.statefulconversation.persistence;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.ObjectSerialisationSupplier;
import ch.zhaw.statefulconversation.model.Response;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.TransitionException;
import ch.zhaw.statefulconversation.model.commons.actions.StaticExtractionAction;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.repositories.StateRepository;
import ch.zhaw.statefulconversation.repositories.StorageRepository;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class ExtractionActionPersistenceTest {

    private static String storageKeyTo;
    private static State state;
    private static String userName;
    private static UUID stateID;

    @BeforeAll
    static void setUp() {
        Storage storage = new Storage();
        ExtractionActionPersistenceTest.storageKeyTo = "name";
        ExtractionActionPersistenceTest.userName = "Mike";
        Decision trigger = new StaticDecision(
                "Examine the following chat and decide if the user mentions their name.");
        Action action = new StaticExtractionAction("Analyse the following conversation and extract the person's name.",
                storage, ExtractionActionPersistenceTest.storageKeyTo);
        Transition transition = new Transition(List.of(trigger), List.of(action), new Final());
        ExtractionActionPersistenceTest.state = new State("You are a grumpy assistant.", "greeting",
                "Say hello and ask for their name.",
                List.of(transition));
    }

    @Autowired
    private StateRepository stateRepository;

    @Test
    @Order(1)
    void save() {
        State stateSaved = this.stateRepository.save(ExtractionActionPersistenceTest.state);
        ExtractionActionPersistenceTest.stateID = stateSaved.getId();
    }

    @Test
    @Order(2)
    void retrieve() {
        Optional<State> stateMaybe = this.stateRepository.findById(ExtractionActionPersistenceTest.stateID);
        assertTrue(stateMaybe.isPresent());
    }

    @Test
    @Order(3)
    void start() {
        Optional<State> stateMaybe = this.stateRepository.findById(ExtractionActionPersistenceTest.stateID);
        assertTrue(stateMaybe.isPresent());
        Response response = stateMaybe.get().start();
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());

        // fix
        this.stateRepository.save(stateMaybe.get());
    }

    @Test
    @Order(4)
    void respond() {
        Optional<State> stateMaybe = this.stateRepository.findById(ExtractionActionPersistenceTest.stateID);
        assertTrue(stateMaybe.isPresent());
        Response response = null;
        try {
            response = stateMaybe.get().respond("My name is useless.");
        } catch (TransitionException e) {
            assertTrue(false);
        }
        assertNotNull(response);
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());

        // fix
        this.stateRepository.save(stateMaybe.get());
    }

    @Test
    @Order(5)
    void transition() {
        Optional<State> stateMaybe = this.stateRepository.findById(ExtractionActionPersistenceTest.stateID);
        assertTrue(stateMaybe.isPresent());
        TransitionException e = assertThrows(TransitionException.class, () -> {
            stateMaybe.get()
                    .respond("My name is " + ExtractionActionPersistenceTest.userName + ".");
        });
        assertNotNull(e.getSubsequentState());
        assertInstanceOf(Final.class, e.getSubsequentState());

        assertEquals(4, stateMaybe.get().getUtterances().toList().size());
        this.stateRepository.save(stateMaybe.get());
    }

    @Autowired
    private StorageRepository storageRepository;

    @Test
    @Order(6)
    void extractStored() {
        List<Storage> storages = this.storageRepository.findAll();
        assertEquals(1, storages.size());
        Storage storage = storages.iterator().next();

        assertTrue(storage.containsKey(ExtractionActionPersistenceTest.storageKeyTo));
        JsonElement extract = storage.get(ExtractionActionPersistenceTest.storageKeyTo);
        assertInstanceOf(JsonObject.class, extract);
        Set<Entry<String, JsonElement>> entrySet = ((JsonObject) extract).entrySet();
        assertEquals(1, entrySet.size(), new ObjectSerialisationSupplier(extract));
        Entry<String, JsonElement> entry = entrySet.iterator().next();
        assertDoesNotThrow(new ThrowingSupplier<String>() {
            @Override
            public String get() throws Throwable {
                return entry.getValue().getAsString();
            }
        }, new ObjectSerialisationSupplier(extract));
        String nameExtracted = entry.getValue().getAsString();
        assertEquals(ExtractionActionPersistenceTest.userName, nameExtracted, new ObjectSerialisationSupplier(extract));
    }

}
