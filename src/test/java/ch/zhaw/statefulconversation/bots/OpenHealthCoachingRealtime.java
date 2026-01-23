package ch.zhaw.statefulconversation.bots;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.OuterState;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.actions.StaticExtractionAction;
import ch.zhaw.statefulconversation.model.commons.actions.TransferUtterancesAction;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.model.commons.states.DynamicActionableCoachingState;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@SpringBootTest
class OpenHealthCoachingRealtime {

        private static final String PROMPT_OUTERSTATE = """
                        You are a supportive health coach.
                        Guide the patient through a comfortable conversation that may end with a practical health related step.
                        Ask only one question at a time.
                        Keep responses brief, one or two sentences.
                        Use plain text only.
                        """;
        private static final String PROMPT_OUTERSTATE_TRIGGER = """
                        Review the user's latest messages in the following conversation.
                        Decide if there are any statements or cues suggesting they wish to pause or stop the conversation, such as explicit requests for a break, indications of needing time, or other phrases implying a desire to end the chat.
                        """;
        private static final String PROMPT_OUTERSTATE_GUARD = """
                        Examine the following conversation and confirm that the patient has not reported any issues like physical or mental discomfort that need addressing.
                        """;

        private static final String PROMPT_RAPPORTBUILDING = """
                        Begin with light, comfortable small talk to help the patient feel at ease.
                        Ask about general well being, hobbies, or weekend plans, avoiding politics and religion.
                        Listen actively and invite sharing.

                        As the patient relaxes, gently shift toward slightly deeper topics without explicitly assessing health goals.
                        Look for clues about what matters to them regarding health or well being, such as valued habits, energizing activities, meaningful experiences, or frustrations.
                        Let this emerge naturally.

                        If the patient appears disengaged, adapt.
                        Treat disengagement as very short replies such as yes, no, ok, or one or two word answers across at least two consecutive questions that invited elaboration.
                        In that case, stop probing and ask one gentle consent question about continuing now versus pausing for later.
                        If they want to stop, respond warmly and do not pressure them.
                        If they want to continue, switch to low effort prompts such as offering a simple choice between two light topics.
                        Ask the consent question at most once unless disengagement happens again.
                        """;
        private static final String PROMPT_RAPPORTBUILDING_STARTER = """
                        Generate a friendly first message to greet the patient and invite light small talk, without mentioning health goals or deeper topics yet.
                        """;
        private static final String PROMPT_RAPPORTBUILDING_TRIGGER = """
                        Decide whether to transition from rapport building to coaching.

                        Return "true" only if both conditions are met.

                        1) A coaching relevant clue exists.
                        The patient expressed a specific health related motivation or driver, not just a generic activity.
                        It must include at least one of why it matters, a desired outcome, a barrier, a habit pattern, or a value statement.

                        2) Rapport grounding exists.
                        There has been at least one back and forth where the assistant reflected or asked a gentle deeper question and the patient confirmed or elaborated with personal detail.

                        Return "false" if the patient only named an activity without meaning or if no grounded back and forth has occurred.
                        """;
        private static final String PROMPT_RAPPORTBUILDING_ACTION = """
                        Identify statements that reveal deeper motivations or health related priorities.
                        Summarize them as JSON in the following format.

                        {
                        "clues": [
                            {
                            "aspect": "<type of clue>",
                            "quote": "<patient statement>",
                            "interpretation": "<brief paraphrase>"
                            }
                        ],
                        "dominant_theme": "<brief theme or None>"
                        }
                        """;

        private static final String PROMPT_FINAL = """
                        This is the final state and the conversation has ended.
                        If the patient sends further messages, do not restart, ask questions, or introduce new topics.
                        Briefly acknowledge the message, state that the conversation is complete, and note that a new session is needed to continue.
                        Keep responses short, warm, and non intrusive.
                        """;
        private static final String PROMPT_FINAL_STARTER = """
                        Generate a brief parting message for the patient.
                        If they have an agreed-upon health action plan, restate it succinctly.
                        Otherwise, wish them well and invite them to reconnect anytime.
                        """;

        @Autowired
        private AgentRepository repository;

        @Test
        void setUp() {
                String storageKeyToGoalDetected = "GoalDetected";
                String storageKeyToActionAgreed = "ActionAgreed";
                Storage storage = new Storage();

                State bestCaseFinal = new Final("Regular Ending Final", OpenHealthCoachingRealtime.PROMPT_FINAL,
                                OpenHealthCoachingRealtime.PROMPT_FINAL_STARTER);
                State coachingState = new DynamicActionableCoachingState(
                                "ActionableCoaching",
                                bestCaseFinal,
                                storage,
                                storageKeyToGoalDetected,
                                storageKeyToActionAgreed);

                Transition fromRapportBuildingToCoaching = new Transition(
                                List.of(new StaticDecision(OpenHealthCoachingRealtime.PROMPT_RAPPORTBUILDING_TRIGGER)),
                                List.of(new StaticExtractionAction(
                                                OpenHealthCoachingRealtime.PROMPT_RAPPORTBUILDING_ACTION,
                                                storage, storageKeyToGoalDetected),
                                                new TransferUtterancesAction(coachingState)),
                                coachingState);
                State rapportBuildingState = new State(
                                OpenHealthCoachingRealtime.PROMPT_RAPPORTBUILDING,
                                "RapportBuilding",
                                OpenHealthCoachingRealtime.PROMPT_RAPPORTBUILDING_STARTER,
                                List.of(fromRapportBuildingToCoaching));

                State finalFromOuter = new Final("User Exit Final");
                Transition userExitTransition = new Transition(
                                List.of(new StaticDecision(OpenHealthCoachingRealtime.PROMPT_OUTERSTATE_TRIGGER),
                                                new StaticDecision(OpenHealthCoachingRealtime.PROMPT_OUTERSTATE_GUARD)),
                                List.of(new TransferUtterancesAction(finalFromOuter)),
                                finalFromOuter);
                State outerState = new OuterState(
                                OpenHealthCoachingRealtime.PROMPT_OUTERSTATE,
                                "OuterState",
                                List.of(userExitTransition),
                                rapportBuildingState);

                Agent agent = new Agent("Wellness Navigator Realtime",
                                "Wellness Navigator Realtime helps patients uncover what matters for their health and transform those insights into actionable self-care steps for everyday well-being.",
                                outerState,
                                storage);
                agent.start();
                this.repository.save(agent);
        }
}
