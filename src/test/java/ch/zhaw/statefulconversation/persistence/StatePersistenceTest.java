package ch.zhaw.statefulconversation.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.TransitionException;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.repositories.StateRepository;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class StatePersistenceTest {

    private static State state;
    private static UUID stateID;

    @BeforeAll
    static void setUp() {
        Decision trigger = new StaticDecision(
                "Examine the following chat and decide if the user mentions their name.");
        Decision guard = new StaticDecision(
                "Examine the following chat and decide if the name given is actually a person's name.");
        Transition transition = new Transition(List.of(trigger, guard), List.of(), new Final());
        StatePersistenceTest.state = new State("You are a grumpy assistant.", "greeting",
                "Say hello and ask for their name.",
                List.of(transition));
    }

    @Autowired
    private StateRepository repository;

    @Test
    @Order(1)
    void save() {
        State stateSaved = this.repository.save(StatePersistenceTest.state);
        StatePersistenceTest.stateID = stateSaved.getId();
    }

    @Test
    @Order(2)
    void retrieve() {
        Optional<State> stateMaybe = this.repository.findById(StatePersistenceTest.stateID);
        assertTrue(stateMaybe.isPresent());
    }

    @Test
    @Order(3)
    void start() {
        Optional<State> stateMaybe = this.repository.findById(StatePersistenceTest.stateID);
        assertTrue(stateMaybe.isPresent());
        String response = stateMaybe.get().start();

        assertNotNull(response);
        assertFalse(response.isEmpty());
        this.repository.save(stateMaybe.get());
    }

    @Test
    @Order(4)
    void respond() {
        Optional<State> stateMaybe = this.repository.findById(StatePersistenceTest.stateID);
        assertTrue(stateMaybe.isPresent());
        String response = null;
        try {
            response = stateMaybe.get().respond("My name is useless.");
        } catch (TransitionException e) {
            assertTrue(false);
        }
        assertNotNull(response);
        assertFalse(response.isEmpty());
        this.repository.save(stateMaybe.get());
    }

    @Test
    @Order(5)
    void transition() {
        Optional<State> stateMaybe = this.repository.findById(StatePersistenceTest.stateID);
        assertTrue(stateMaybe.isPresent());
        TransitionException e = assertThrows(TransitionException.class, () -> {
            stateMaybe.get().respond("My name is mike.");
        });
        assertNotNull(e.getSubsequentState());
        assertInstanceOf(Final.class, e.getSubsequentState());
        this.repository.save(stateMaybe.get());
    }

    @Test
    @Order(6)
    void reset() {
        Optional<State> stateMaybe = this.repository.findById(StatePersistenceTest.stateID);
        assertTrue(stateMaybe.isPresent());
        stateMaybe.get().reset();
        assertTrue(stateMaybe.get().getUtterances().toList().isEmpty());
        this.repository.save(stateMaybe.get());
    }

    @Test
    @Order(7)
    void restart() {
        Optional<State> stateMaybe = this.repository.findById(StatePersistenceTest.stateID);
        assertTrue(stateMaybe.isPresent());
        String response = stateMaybe.get().start();

        assertNotNull(response);
        assertFalse(response.isEmpty());
        this.repository.save(stateMaybe.get());
    }
}
