package ch.zhaw.statefulconversation.bots;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.actions.StaticExtractionAction;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@SpringBootTest
class SingleStateInteraction {

        private static final String PROMPT_STATE = """
                        As a digital therapy coach, check in daily with your patient to assess their well-being related to their chronic condition.
                        Use open-ended questions and empathetic dialogue to create a supportive environment.
                        Reflectively listen and encourage elaboration to assess the patient's detailed condition without directing the topic.
                        Recognize and affirm their achievements, offer support on tough days, and understand any specific issues without suggesting therapy changes.
                        Your role is to assess and understand, not to advise or alter their therpy plan.
                        Always respond with very brief, succinct answers, keeping them to a maximum of one or two sentences.
                        Once the patient shared a couple of thoughts, end the conversation with a brief summary of what the patient reported, and whish them well.
                        Meet Daniel Müller, 52, who is tackling obesity with a therapy plan that includes morning-to-noon intermittent fasting, thrice-weekly 30-minute swims, and a switch to whole grain bread.
                        """;
        private static final String PROMPT_STATE_STARTER = """
                        After reviewing the patient's profile, compose a single, very short message that the therapy coach would use to initiate today's check-in conversation with Daniel.
                        """;
        private static final String PROMPT_TRIGGER = """
                        Review the patient's latest messages in the following conversation.
                        Decide if there are any statements or cues suggesting they wish to pause or stop the conversation, such as explicit requests for a break, indications of needing time, or other phrases implying a desire to end the chat.
                        """;
        private static final String PROMPT_GUARD = """
                        Examine the following conversation and confirm that the patient has not reported any issues like physical or mental discomfort that have not been addressed yet.
                        """;
        private static final String PROMPT_ACTION = """
                        Summarize the coach-patient conversation, highlighting adherence to the therapy plan, the patient's attitude, and well-being for the physician's review.
                        """;
        @Autowired
        private AgentRepository repository;

        @Test
        void setUp() {
                Storage storage = new Storage();
                Decision trigger = new StaticDecision(
                                SingleStateInteraction.PROMPT_TRIGGER);
                Decision guard = new StaticDecision(
                                SingleStateInteraction.PROMPT_GUARD);
                Action action = new StaticExtractionAction(SingleStateInteraction.PROMPT_ACTION, storage, "summary");
                Transition transition = new Transition(List.of(trigger, guard), List.of(action),
                                new Final("User Exit Final"));
                State state = new State(SingleStateInteraction.PROMPT_STATE, "Check-In Interaction",
                                SingleStateInteraction.PROMPT_STATE_STARTER,
                                List.of(transition));
                Agent agent = new Agent("Digital Companion", "Daily check-in conversation.", state);
                agent.start();
                this.repository.save(agent);
        }
}
