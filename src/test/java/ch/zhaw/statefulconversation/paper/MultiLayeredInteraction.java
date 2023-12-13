package ch.zhaw.statefulconversation.paper;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.OuterState;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.actions.RemoveLastUtteranceAction;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@SpringBootTest
class MultiLayeredInteraction {

        @Autowired
        private AgentRepository repository;

        @Test
        void setUp() {
                // 1. Inner Machine, Simulation: [Acting]-->[Debriefing]-->[Final]
                State debriefing = new State(
                                "You now act as the coach. Compliment the physician on their compassionate approach during the simulated patient interaction. Acknowledge the positive impact this approach would have in real patient consultations.",
                                "debriefing",
                                "Using a conversational tone, make the compliment now with one brief verbal statement that ends with you saying goodbye.",
                                List.of(new Transition(new Final())));
                Transition actingToDebriefing = new Transition(new StaticDecision(
                                "Examine the physician's behavior in the following conversation with the patient. Assess if they demonstrated any form of compassion, considering either their empathy, understanding, or supportive communication."),
                                debriefing);
                State acting = new State(
                                "You now act as the fictitious 45-year-old patient named Alex struggling with long-term obesity. Despite various diets and exercises, your weight issues persist, leading to frustration and hopelessness. In your appointment, express these challenges, focusing on difficulties with diet, exercise, and emotional well-being. Seek advice and support from the physician, responding realistically to their suggestions.",
                                "acting",
                                "Now, start the conversation with the physician with a short statement expressing your main concern about your struggle with obesity.",
                                List.of(actingToDebriefing));

                // 2. Outer Machine, Observing Coach: [Simulation]-->[Intervene]-->[Simulation]
                Transition outerInterveneToOuterplay = new Transition(new StaticDecision(
                                "Review the conversation between the physician and coach. Determine if the physician opts to continue with the patient after the feedback."), null);
                State outerIntervene = new State(
                                "You now act as the coach. Respectfully notify the physician about the need for more compassion in their interaction. Suggest improvements like enhanced empathetic listening and supportive responses. After giving feedback, ask the physician if they want to continue with the patient using these tips, or if they need more advice on compassionate communication.",
                                "outerIntervene",
                                "Using a conversational tone, notify the physician now with one brief verbal statement.",
                                List.of(outerInterveneToOuterplay));
                Transition outerplayToOuterIntervene = new Transition(new StaticDecision(
                                "Examine the physician's behavior in the following conversation with the patient. Determine if they lacked compassion, focusing on instances of insufficient empathy, understanding, or supportive communication."),
                                outerIntervene);
                State outerPlay = new OuterState(
                                null,
                                "outerPlay",
                                List.of(outerplayToOuterIntervene), acting);
                // the following lines are a bit of a hack due to circularity...
                outerInterveneToOuterplay.addAction(new RemoveLastUtteranceAction(outerPlay));
                outerInterveneToOuterplay.setSubsequenState(outerPlay);

                // 3. Outermost Machine, Prompt Inheritance and User Exit: [Outermost]-->[Final]
                Transition botToFinal = new Transition(new StaticDecision(
                                "Review the physician's latest messages in the following conversation. Decide if there are any statements or cues suggesting they wish to pause or stop the conversation, such as explicit requests for a break, indications of needing time, or other phrases implying a desire to end the coaching session."),
                                new Final());
                State outermostState = new OuterState(
                                "You are acting as either as a coach or as a fictitious patient, but never both simultaneously7. If acting as the patient, simulate a health issue scenario. If acting as the coach, provide brief, focused feedback to improve the physician's compassionate communication. Always respond with very brief, succinct answers, limited to one or two sentences. Do not create dialogues involving multiple roles. Never indicate a role in any response.",
                                "outermostState", List.of(botToFinal), outerPlay);

                Agent agent = new Agent("Medical Consultation Coach",
                                "Supports physicians to improve their conversational behaviour during consultations.",
                                outermostState);
                agent.start();
                this.repository.save(agent);
        }
}
