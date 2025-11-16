package ch.zhaw.statefulconversation.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
class AgentTest {

    private static Agent agent;

    @BeforeAll
    static void setUp() {
        Decision trigger = new StaticDecision(
                "Examine the following chat and decide if the user mentions their name.");
        Transition transition = new Transition(List.of(trigger), List.of(), new Final());
        State state = new State("You are a grumpy assistant.", "greeting", "Say hello and ask for their name.",
                List.of(transition));
        AgentTest.agent = new Agent("Grumpy Assistant", "Grumpy assistant trying to obtain your name.", state);
    }

    @Test
    @Order(1)
    void start() {
        Response response = AgentTest.agent.start();
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());
    }

    @Test
    @Order(2)
    void respond() {
        Response response = AgentTest.agent.respond("my name is useless.");
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());
    }

    @Test
    @Order(3)
    void ending() {
        Response response = AgentTest.agent.respond("my name is mike.");
        assertNotNull(response.getText());
        assertFalse(response.getText().isEmpty());
        assertFalse(AgentTest.agent.isActive());
    }
}
