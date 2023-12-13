package ch.zhaw.statefulconversation.bots;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.OuterState;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@SpringBootTest
class BotInALoop {

        @Autowired
        private AgentRepository repository;

        @Test
        void setUp() {

                Decision ensureThanksHappend = new StaticDecision(
                                "Return true if the Assistant has thanked the user for filling in the diary. Return false if the Assistant has not yet thanked the user.");
                Transition finishToCheckIn = new Transition(List.of(ensureThanksHappend), List.of(), null);
                State Finish = new State("Bedanke dich beim User, dass er seinen Eintrag abgeschlossen hat.",
                                "Finish", "Bedanke dich nun.", List.of(finishToCheckIn));
                Decision trigger = new StaticDecision(
                                "Analysiere die folgende Konversation und entscheide folgendes: \n - der User wurde gefragt, ob er geduzt oder gesiezt werden will \n - Der User wurde gefragt wie es ihm geht.\n Beide Bedingungen müssen erfüllt werden.");
                Transition checkInToFinish = new Transition(List.of(trigger), List.of(), Finish);
                State checkIn = new State(
                                "Führe Small Talk mit dem Benutzer. Begrüße ihn herzlich und frage, ob er geduzt oder gesiezt werden möchte. Erkundige dich, wie es ihm geht. Stelle immer nur eine Frage auf einmal und gehe auf seine Antworten ein, um eine angenehme Konversation aufzubauen und Nähe zu schaffen.",
                                "checkIn",
                                "Starte nun das Gespräch mit deiner ersten Frage.",
                                List.of(checkInToFinish));
                finishToCheckIn.setSubsequenState(checkIn); // it's a loop, so we have to set this here.
                Transition outerinterveneToOuterplay = new Transition(List.of(), List.of(), null);
                State outerIntervene = new State(
                                "Sag dem Patienten, dass es noch offene Punkte gibt.",
                                "outerIntervene",
                                "Sag dies nun dem Patienten.",
                                List.of(outerinterveneToOuterplay));

                Decision outerTrigger = new StaticDecision(
                                "Examine the following conversation and return true if the patient has not reported any issues. Return false if the patient reported issues like physical or mental discomfort that needs addressing.");
                Transition outerplayToOuterintervene = new Transition(List.of(outerTrigger), List.of(), outerIntervene);
                State outerPlay = new OuterState(
                                null,
                                "outerPlay",
                                List.of(outerplayToOuterintervene), checkIn);
                outerinterveneToOuterplay.setSubsequenState(outerPlay);

                Transition botToFinal = new Transition(List.of(new StaticDecision(
                                "Review the patient's latest messages in the following conversation. Decide if there are any statements or cues suggesting they wish to pause or stop the conversation, such as explicit requests for a break, indications of needing time, or other phrases implying a desire to end the chat..")),
                                List.of(), checkIn);
                final String PROMPT_OUTER = """
                                        As a digital therapy coach, check in daily with your patient to assess their well-being related to their chronic condition.
                                Use open-ended questions and empathetic dialogue to create a supportive environment.
                                Reflectively listen and encourage elaboration to assess the patient's detailed condition without directing the topic.
                                Recognize and affirm their achievements, offer support on tough days, and understand any specific issues without suggesting therapy changes.
                                Your role is to assess and understand, not to advise or alter their therpy plan.
                                Always respond with very short, clear answers to maintain brevity in communication. Aim for responses that contain no more than roughly 10 tokens.
                                Once the patient shared a couple of thoughts, end the conversation with a brief summary of what the patient reported, and whish them well.
                                Meet Daniel Müller, 52 who is tackling obesity with a therapy plan that includes morning-to-noon intermittent fasting, thrice-weekly 30-minute swims, and a switch to whole grain bread.""";
                State outermostState = new OuterState(PROMPT_OUTER, "outermostState", List.of(botToFinal), outerPlay);
                Agent agent = new Agent("botInALoop",
                                "Prototype of a bot that can ask daily questions.", outermostState);
                agent.start();
                this.repository.save(agent);
        }
}
