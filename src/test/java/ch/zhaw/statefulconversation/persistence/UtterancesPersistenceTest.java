package ch.zhaw.statefulconversation.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.zhaw.statefulconversation.model.Utterance;
import ch.zhaw.statefulconversation.model.Utterances;
import ch.zhaw.statefulconversation.repositories.UtterancesRepository;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
public class UtterancesPersistenceTest {

    private static final String userSays1 = "Hello assistant";
    private static final String assistantSays = "Hello user";

    private static UUID utterancesID;

    @Autowired
    UtterancesRepository repository;

    @Test
    @Order(1)
    void testSave() {
        Utterances utterances = new Utterances();
        assertTrue(utterances.isEmpty());

        utterances.appendUserSays(userSays1);
        assertFalse(utterances.isEmpty());
        List<Utterance> list = utterances.toList();
        assertEquals(1, list.size());

        utterances = this.repository.save(utterances);
        UtterancesPersistenceTest.utterancesID = utterances.getID();
    }

    @Test
    @Order(2)
    void testRetrieve() {
        Optional<Utterances> utterancesMaybe = this.repository.findById(UtterancesPersistenceTest.utterancesID);
        assertTrue(utterancesMaybe.isPresent());
        assertFalse(utterancesMaybe.get().isEmpty());
        List<Utterance> list = utterancesMaybe.get().toList();
        assertEquals(1, list.size());
    }

    @Test
    @Order(3)
    void testUpdate() {
        Optional<Utterances> utterancesMaybe = this.repository.findById(UtterancesPersistenceTest.utterancesID);
        assertTrue(utterancesMaybe.isPresent());
        utterancesMaybe.get().appendAssistantSays(UtterancesPersistenceTest.assistantSays);

        assertFalse(utterancesMaybe.get().isEmpty());
        List<Utterance> list = utterancesMaybe.get().toList();
        assertEquals(2, list.size());
        this.repository.save(utterancesMaybe.get());
    }

    @Test
    @Order(4)
    void testUpdateRetrieve() {
        Optional<Utterances> utterancesMaybe = this.repository.findById(UtterancesPersistenceTest.utterancesID);
        assertTrue(utterancesMaybe.isPresent());
        assertFalse(utterancesMaybe.get().isEmpty());
        List<Utterance> list = utterancesMaybe.get().toList();
        assertEquals(2, list.size());
    }

    @Test
    @Order(5)
    void testReset() {
        Optional<Utterances> utterancesMaybe = this.repository.findById(UtterancesPersistenceTest.utterancesID);
        assertTrue(utterancesMaybe.isPresent());

        utterancesMaybe.get().reset();

        assertTrue(utterancesMaybe.get().isEmpty());
        List<Utterance> list = utterancesMaybe.get().toList();
        assertEquals(0, list.size());

        this.repository.save(utterancesMaybe.get());
    }

    @Test
    @Order(6)
    void testResetRetrieve() {
        Optional<Utterances> utterancesMaybe = this.repository.findById(UtterancesPersistenceTest.utterancesID);
        assertTrue(utterancesMaybe.isPresent());

        assertTrue(utterancesMaybe.get().isEmpty());
        List<Utterance> list = utterancesMaybe.get().toList();
        assertEquals(0, list.size());
    }
}
