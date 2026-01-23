package ch.zhaw.statefulconversation.model.commons.states;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.actions.StaticExtractionAction;
import ch.zhaw.statefulconversation.model.commons.actions.TransferUtterancesAction;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.utils.NamedParametersFormatter;
import jakarta.persistence.Entity;

@Entity
public class DynamicActionableCoachingState extends State {

        private static final String PROMPT_BEFORE = "The patient has just revealed the following clues regarding a key health-related goal: ";
        private static final String PROMPT_AFTER = """
                        Guide the patient to define one practical health action for this week that aligns with this goal.
                        Help refine it until it is realistic, motivating, and SMART.
                        Offer support, examples, and obstacle planning.
                        Maintain an empathetic tone.
                        """;
        private static final String STARTER_PROMPT = "Generate a brief message that acknowledges the patient's newly identified health goal and invites them to explore a single practical action they can take this week to improve their well-being.";
        private static final String TRIGGER = """
                        Analyze the following conversation and determine if the patient has committed to a specific, realistic action to implement their newly identified health goal in their daily self-care or well-being routine.
                        Return \"true\" if such an action is clearly stated and the patient agrees to it. Otherwise, return \"false.\"
                        """;;
        private static final String ACTION = """
                        Analyze the following conversation and identify the specific health action the patient agreed to undertake, ensuring it reflects a SMART plan (Specific, Measurable, Achievable, Relevant, Time-bound).
                        Summarize the plan in JSON format with clear attributes, for example:
                        {
                                \"action\": {
                                        \"description\": \"..\",
                                        \"timeline\": \"...\",
                                        \"measure_of_success\": \"...\",
                                        \"relevance_to_health_goal\": \"...\",
                                        \"obstacles_or_support_needed\": \"...\"
                                }
                        }
                        """;

        protected DynamicActionableCoachingState() {

        }

        public DynamicActionableCoachingState(String name, State subsequentState,
                        Storage storage,
                        String storageKeyFrom,
                        String storageKeyTo) {
                this(name, subsequentState, storage, storageKeyFrom, storageKeyTo, true, false);
        }

        public DynamicActionableCoachingState(String name, State subsequentState,
                        Storage storage,
                        String storageKeyFrom,
                        String storageKeyTo,
                        boolean isStarting,
                        boolean isOblivious) {
                super(DynamicActionableCoachingState.PROMPT_BEFORE + "${" + storageKeyFrom + "}"
                                + DynamicActionableCoachingState.PROMPT_AFTER,
                                name,
                                DynamicActionableCoachingState.STARTER_PROMPT,
                                List.of(),
                                DynamicActionableCoachingState.ACTION,
                                isStarting,
                                isOblivious,
                                storage,
                                List.of(storageKeyFrom));
                Decision trigger = new StaticDecision(DynamicActionableCoachingState.TRIGGER);
                Action action = new StaticExtractionAction(
                                DynamicActionableCoachingState.ACTION,
                                storage,
                                storageKeyTo);
                Transition transition = new Transition(List.of(trigger),
                                List.of(action, new TransferUtterancesAction(subsequentState)), subsequentState);
                this.addTransition(transition);
        }

        @Override
        protected String getPrompt() {
                Map<String, JsonElement> valuesForKeys = this.getValuesForKeys();
                if (!(valuesForKeys.values().iterator().next() instanceof JsonObject)) {
                        throw new RuntimeException(
                                        "expected storageKeyFrom being associated to an object (JsonObject) but enountered "
                                                        + valuesForKeys.values().iterator().next().getClass()
                                                        + " instead");
                }

                return NamedParametersFormatter.format(super.getPrompt(), valuesForKeys);
        }

        @Override
        public String toString() {
                return "DynamicActionableCoachingState IS-A " + super.toString();
        }
}
