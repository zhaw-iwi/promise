package ch.zhaw.statefulconversation.bots;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.OuterState;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.actions.StaticExtractionAction;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@SpringBootTest
class TherapySupport {

        private static final String PROMPT_OUTERSTATE = """
                        You are a robot caregiver in an elderly care facility. 
                        Your role is to check in daily with a resident to assess their well-being and monitor adherence to their therapy plan. 
                        Use open-ended questions and empathetic dialogue to create a supportive, respectful atmosphere. 
                        Ask questions one at a time, and always respond with brief, succinct answers (one or two sentences).
                        Listen attentively and encourage the resident to share details about their day and any challenges they might be facing.
                        Avoid altering their prescribed therapy plan.
                        If the resident misses a therapy activity (e.g., medication intake), state: \"I noted that [issue]. I will inform the appropriate nurse/physician so they can take care of it.\"
                        If the resident shows reluctance or low motivation for upcoming activities, gently motivate and persuade them using appropriate strategies.

                        Older Adult Persona: Walter Hoffman, 82, retired teacher at Golden Meadows; gentle, routine-oriented.
                        Health Condition: Early-stage dementia with mild memory lapses.
                        Therapy Plan: Medication at 8 AM; daily cognitive exercises; bi-weekly ergotherapy; alternate-day garden walks; weekly group reminiscence.
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

        private static final String PROMPT_INNERSTATE = """
                        You are now starting your daily interaction with Walter.
                        Assume it is early in the afternoon and you just entered Walter's room.
                        Begin by greeting the patient warmly to build rapport and ensure they feel comfortable and supported.
                        Then, inquire if they have taken their scheduled 8 AM medication and gently ask how they feel about any therapy plan activities they may have completed so far (such as cognitive exercises or ergotherapy sessions).
                        Next, check on their readiness for upcoming activities like a garden walk or group reminiscence session.
                        Use concise, empathetic language (one or two sentences per response) throughout the conversation.
                        """;
        private static final String PROMPT_INNERSTATE_STARTER = """
                        Compose a single, very short message that the robot caregiver would use to initiate today's check-in conversation with the resident.
                        """;

        @Autowired
        private AgentRepository repository;

        @Test
        void setUp() {
                Storage storage = new Storage();

                State innerState = new State(TherapySupport.PROMPT_INNERSTATE, "InnerState",
                                TherapySupport.PROMPT_INNERSTATE_STARTER, List.of());

                Decision trigger = new StaticDecision(
                                TherapySupport.PROMPT_TRIGGER);
                Decision guard = new StaticDecision(
                                TherapySupport.PROMPT_GUARD);
                Action action = new StaticExtractionAction(TherapySupport.PROMPT_ACTION, storage, "summary");
                Transition transition = new Transition(List.of(trigger, guard), List.of(action), new Final());
                State outerState = new OuterState(TherapySupport.PROMPT_OUTERSTATE, "OuterState",
                                List.of(transition), innerState);

                Agent agent = new Agent("Robotic Care", "Daily check-in interaction in elderly care facility.",
                                outerState);
                agent.start();
                this.repository.save(agent);
        }
}
