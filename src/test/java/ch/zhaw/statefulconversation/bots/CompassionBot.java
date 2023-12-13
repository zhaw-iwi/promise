package ch.zhaw.statefulconversation.bots;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.OuterState;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@SpringBootTest
class CompassionBot {

        @Autowired
        private AgentRepository repository;

        @Test
        void setUp() {

                Transition debriefingToFinal = new Transition(List.of(), List.of(), new Final());
                State debriefing = new State("So, as a coach, you tell the user that they have done a good job.",
                                "debriefing", "Let the user know.", List.of(debriefingToFinal));
                Decision trigger = new StaticDecision(
                                "Analyse the following conversation and decide if the user is showing some form of compassion for the fictitious person.");
                Transition actingToDebriefing = new Transition(List.of(trigger), List.of(), debriefing);
                State acting = new State(
                                "So, as a fictitious person, you are now to invent something bad that happened to you.",
                                "acting",
                                "Now, talk about the bad thing that just happened to you. Do not mention that you are a fictitious person",
                                List.of(actingToDebriefing));

                Transition outerinterveneToOuterplay = new Transition(List.of(), List.of(), null);
                State outerIntervene = new State(
                                "As a coach, you now tell the user that they have not shown much compassion for the fictitious person so far. You give the user a tip on how they can show more compassion in their conversation with the fictitious person.",
                                "outerIntervene",
                                "Tell the user that now.",
                                List.of(outerinterveneToOuterplay));

                Decision outerTrigger = new StaticDecision(
                                "Analyse the following conversation and decide if the user shows a lack of compassion for the fictitious person. Return False if the user asks questions to clarify the issue or to find out how they could help. Return True if the user loughs at the issue, is rude or if they start giving advice instead of listening or acknowledging the issue.");
                Transition outerplayToOuterintervene = new Transition(List.of(outerTrigger), List.of(), outerIntervene);
                State outerPlay = new OuterState(
                                null,
                                "outerPlay",
                                List.of(outerplayToOuterintervene), acting);
                // outerinterveneToOuterplay.action(null); =
                // RemoveLastUtteranceAction(outer_play) --> we don't have this action yet
                outerinterveneToOuterplay.setSubsequenState(outerPlay);

                Transition botToFinal = new Transition(List.of(new StaticDecision(
                                "Analyse the following conversation and decide if the user is signaling that they no longer want to participate in the conversation.")),
                                List.of(), new Final());
                State outermostState = new OuterState("""
                                You are a coach who guides the user to be more compassionate.
                                The following describes how the coaching session should proceed:
                                You take on two roles. In one role, you play a fictitious person who is not doing well. In the other role, you are the coach.
                                As the coach, you observe the conversation that the user has with the fictitious person.
                                As the coach, you give the user tips on how to be more compassionate when the user is not behaving compassionately.
                                The user never talks to you, but always and only to the fictitious person.
                                Always be brief in all conversations. Be brief as the coach. Be brief as the fictitious person.
                                (end of description)\
                                """, "outermostState", List.of(botToFinal), outerPlay);
                Agent agent = new Agent("compassionBot",
                                "Teaches the user to be compassionate with fictional scenarios.", outermostState);
                agent.start();
                this.repository.save(agent);
        }
}
