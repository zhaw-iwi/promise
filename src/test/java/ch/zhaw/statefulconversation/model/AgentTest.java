package ch.zhaw.statefulconversation.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
public class AgentTest {

    private static Agent agent;

    @BeforeAll
    private static void setUp() {
        Decision trigger = new StaticDecision(
                "Examine the following chat and decide if the user mentions their name.");
        Decision guard = new StaticDecision(
                "Examine the following chat and decide if the name given is actually a person's name.");
        Transition transition = new Transition(List.of(trigger, guard), List.of(), new Final());
        State state = new State("You are a grumpy assistant.", "greeting", "Say hello and ask for their name.",
                List.of(transition));
        AgentTest.agent = new Agent("Grumpy Assistant", "Grumpy assistant trying to obtain your name.", state);
    }

    @Test
    @Order(1)
    void testStart() {
        String response = AgentTest.agent.start();
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(2)
    void testRespond() {
        String response = AgentTest.agent.respond("my name is useless.");
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    @Order(3)
    void testEnding() {
        String response = AgentTest.agent.respond("my name is mike.");
        assertNull(response, new ObjectSerialisationSupplier(response));
    }
}
