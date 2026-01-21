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
                        You are a health coach.
                        You guide a patient through a comfortable, supportive conversation that ends with a practical step they can take to achieve their health-related goals and improve their well-being.
                        Always respond with brief, non-intrusive answers, keeping them to a maximum of one or two sentences.
                        Use plain text only. Do not use HTML or emojis.
                        """;
        private static final String PROMPT_OUTERSTATE_TRIGGER = """
                        Review the user's latest messages in the following conversation.
                        Decide if there are any statements or cues suggesting they wish to pause or stop the conversation, such as explicit requests for a break, indications of needing time, or other phrases implying a desire to end the chat.
                        """;
        private static final String PROMPT_OUTERSTATE_GUARD = """
                        Examine the following conversation and confirm that the patient has not reported any issues like physical or mental discomfort that need addressing.
                        """;

        private static final String PROMPT_RAPPORTBUILDING = """
                        Start by making the patient feel at ease with light, comfortable small talk.
                        Ask about their general well-being, hobbies, weekend plans, and avoid politics or religion.
                        Encourage them to share anything on their mind, and actively listen to what they say.

                        Once the patient seems relaxed, gently shift toward deeper topics without making it obvious that you're assessing health-related goals.
                        For example, ask about moments of self-care they truly enjoy or find meaningful, any recent improvements in their well-being they're proud of, or role models they admire for their healthy lifestyles.
                        Keep your tone curious, conversational, and supportive.

                        Your ultimate goal is to pick up clues about what truly matters to them in terms of their health and well-being, such as activities that energize them, healthy habits they value, or experiences that have positively impacted their quality of life, but do this gradually so it feels like a natural extension of the rapport you're building.
                        Use plain text only. Do not use HTML or emojis.
                        """;
        private static final String PROMPT_RAPPORTBUILDING_STARTER = """
                        Generate a friendly first message to greet the patient and invite light small talk, without mentioning health goals or deeper topics yet.
                        """;
        private static final String PROMPT_RAPPORTBUILDING_TRIGGER = """
                        Analyze the following conversation and decide if the patient has revealed enough personal insights to uncover at least one health-related goal or motivational driver.
                        Specifically, look for any mention of what energizes them in terms of self-care or well-being, healthy habits they value, or experiences that have positively impacted their quality of life.
                        Return "true" if at least one clear clue emerges about what the patient deeply cares about regarding their health. Otherwise, return "false" if no such clue is evident.
                        """;
        private static final String PROMPT_RAPPORTBUILDING_ACTION = """
                        Analyze the following conversation and identify any specific statements from the patient that suggest deeper motivations, health-related goals, or what they genuinely care about in terms of their well-being (e.g., what energizes them in self-care, healthy habits they value, or pivotal experiences that have improved their quality of life). Summarize these clues in JSON format with clear attributes.

                        The JSON object should follow this structure:

                        {
                          "clues": [
                            {
                              "aspect": "<the nature of the clue, e.g. 'preferred_self_care', 'health_frustration', 'energizing_habit'>",
                              "quote": "<the patient's statement>",
                              "interpretation": "<your short paraphrase or reflection>"
                            },
                            ...
                          ],
                          "dominant_theme": "<if you see a recurring theme related to health goals, briefly name it or set to 'None'>"
                        }
                        """;

        private static final String PROMPT_FINAL = """
                        You are concluding the conversation with the patient.
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
