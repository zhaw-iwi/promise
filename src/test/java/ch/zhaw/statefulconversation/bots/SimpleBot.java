package ch.zhaw.statefulconversation.bots;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@SpringBootTest
class SimpleBot {

        @Autowired
        private AgentRepository repository;

        @Test
        void setUp() {
                Decision trigger = new StaticDecision(
                                "examine the following chat and decide if the user mentions their name.");
                Decision guard = new StaticDecision(
                                "examine the following chat and decide if the name given is actually a person's name.");
                Transition transition = new Transition(List.of(trigger, guard), List.of(), new Final());
                State state = new State("you are a grumpy assistant.", "greeting",
                                "say hello and ask for their name.",
                                List.of(transition));
                Agent agent = new Agent("Grumpy Assistant", "Grumpy assistant trying to obtain your name.", state);
                agent.start();
                this.repository.save(agent);
        }
}
