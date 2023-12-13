package ch.zhaw.statefulconversation.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class StateTest {

    private static State state;

    @BeforeAll
    static void setUp() {
        Decision trigger = new StaticDecision(
                "examine the following chat and decide if the user mentions their name.");
        Decision guard = new StaticDecision(
                "examine the following chat and decide if the name given is actually a person's name.");
        Transition transition = new Transition(List.of(trigger, guard), List.of(), new Final());
        StateTest.state = new State("you are a grumpy assistant.", "greeting", "say hello and ask for their name.",
                List.of(transition));
    }

    @Test
    @Order(1)
    void start() {
        String response = StateTest.state.start();
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(2)
    void respond() {
        String response = null;
        try {
            response = StateTest.state.respond("My name is useless.");
        } catch (TransitionException e) {
            assertTrue(false);
        }
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(3)
    void transition() {
        TransitionException e = assertThrows(TransitionException.class, () -> {
            StateTest.state.respond("My name is mike.");
        });
        assertNotNull(e.getSubsequentState());
        assertInstanceOf(Final.class, e.getSubsequentState());
    }
}
