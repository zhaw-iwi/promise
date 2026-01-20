package ch.zhaw.statefulconversation.bots;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.Gson;

import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.OuterState;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.actions.StaticExtractionAction;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.model.commons.states.DynamicSingleChoiceState;
import ch.zhaw.statefulconversation.model.commons.states.EN_DynamicCauseAssessmentState;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@SpringBootTest
class MultiStateInteraction {

        private static final String PROMPT_THERAPYCOACH = """
                        As a digital therapy coach, your role is to support and enhance patient adherence to their therapy plans.
                        Always respond with very brief, succinct answers, keeping them to a maximum of one or two sentences.
                        """;
        private static final String PROMPT_THERAPYCOACH_TRIGGER = """
                        Review the patient's latest messages in the following conversation.
                        Decide if there are any statements or cues suggesting they wish to pause or stop the conversation, such as explicit requests for a break, indications of needing time, or other phrases implying a desire to end the chat.
                        """;
        private static final String PROMPT_THERAPYCOACH_GUARD = """
                        Examine the following conversation and confirm that the patient has not reported any issues like physical or mental discomfort that need addressing.
                        """;
        private static final String PROMPT_THERAPYCOACH_ACTION = """
                        Summarize the coach-patient conversation, highlighting adherence to the therapy plan, issues reported, and suggestions for improvements accepted for the physician's review.
                        """;

        @Autowired
        private AgentRepository repository;

        @Test
        void setUp() {
                String storageKeyFromActivityMissed = "ActivityMissed";
                String storageKeyToReasonProvided = "ReasonProvided";
                String storageKeyFromSuggestionsOffered = "SuggestionsOffered";
                String storageKeyToSuggestionChosen = "SuggestionChosen";

                Gson gson = new Gson();

                Storage storage = new Storage();
                storage.put(storageKeyFromActivityMissed,
                                gson.toJsonTree(List.of("Patient missed 30 minutes of swimming yesterday evening.")));
                storage.put(storageKeyFromSuggestionsOffered,
                                gson.toJsonTree(List.of(
                                                "Less Crowded Swim Sessions: Recommend that the patient look for less busy times to swim at the public pool.",
                                                "Alternative Water Exercises: Propose looking into water aerobics classes which often attract people of all body types, promoting a more inclusive and less self-conscious atmosphere.")));

                State patientChoosesSuggestion = new DynamicSingleChoiceState("PatientChoosesSuggestion",
                                new Final("Suggestion Chosen Final"),
                                storage,
                                storageKeyFromSuggestionsOffered, storageKeyToSuggestionChosen);
                State patientProvidesReason = new EN_DynamicCauseAssessmentState("PatientProvidesReason",
                                patientChoosesSuggestion,
                                storage,
                                storageKeyFromActivityMissed, storageKeyToReasonProvided);

                Transition therapyCoachTransition = new Transition(
                                List.of(new StaticDecision(MultiStateInteraction.PROMPT_THERAPYCOACH_TRIGGER),
                                                new StaticDecision(MultiStateInteraction.PROMPT_THERAPYCOACH_GUARD)),
                                List.of(new StaticExtractionAction(MultiStateInteraction.PROMPT_THERAPYCOACH_ACTION,
                                                storage, storageKeyToSuggestionChosen)),
                                new Final("User Exit Final"));
                State therapyCoach = new OuterState(
                                MultiStateInteraction.PROMPT_THERAPYCOACH,
                                "TherapyCoach",
                                List.of(therapyCoachTransition),
                                patientProvidesReason);

                Agent agent = new Agent("Digital Companion",
                                "Missed activity and adherence improvement.", therapyCoach,
                                storage);
                agent.start();
                this.repository.save(agent);
        }
}
