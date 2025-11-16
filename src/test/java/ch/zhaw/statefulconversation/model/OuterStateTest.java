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
class OuterStateTest {

    private static State state;

    @BeforeAll
    static void setUp() {
        Decision innerStateTrigger = new StaticDecision(
                "Examine the following chat and decide if the user mentions their name.");
        Decision innerStateGuard = new StaticDecision(
                "Examine the following chat and decide if the name given is actually a person's name.");
        Transition innerStateTransition = new Transition(List.of(innerStateTrigger, innerStateGuard), List.of(),
                new Final());
        State innerState = new State("chat with a user.", "greeting", "say hello and ask for their name.",
                List.of(innerStateTransition));
        Decision outerStateTrigger = new StaticDecision("Review the chat and determine if the user wants to exit.");
        Transition outerStateTransition = new Transition(List.of(outerStateTrigger), List.of(), new Final());
        OuterStateTest.state = new OuterState("You are a grumpy assistant.", "conversation",
                List.of(outerStateTransition),
                innerState);
    }

    @Test
    @Order(1)
    void start() {
        Response response = OuterStateTest.state.start();
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());
    }

    @Test
    @Order(2)
    void respond() {
        Response response = null;
        try {
            response = OuterStateTest.state.respond("my name is useless.");
        } catch (TransitionException e) {
            assertTrue(false);
        }
        assertNotNull(response);
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());
    }

    @Test
    @Order(3)
    void outerTransition() {
        TransitionException e = assertThrows(TransitionException.class, () -> {
            OuterStateTest.state.respond("I no longer wish to chat.");
        });
        assertNotNull(e.getSubsequentState());
        assertInstanceOf(Final.class, e.getSubsequentState());
    }
}
