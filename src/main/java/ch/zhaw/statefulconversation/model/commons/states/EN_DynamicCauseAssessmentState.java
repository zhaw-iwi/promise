package ch.zhaw.statefulconversation.model.commons.states;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.actions.DynamicExtractionAction;
import ch.zhaw.statefulconversation.model.commons.decisions.DynamicDecision;
import ch.zhaw.statefulconversation.utils.NamedParametersFormatter;
import jakarta.persistence.Entity;

@Entity
public class EN_DynamicCauseAssessmentState extends State {

        private static final String CAUSEASSESSMENT_PROMPT_PREFIX = "Initiate a conversation to inquire about the patient's reasons for: ";
        private static final String CAUSEASSESSMENT_PROMPT_POSTFIX = "Encourage them to openly discuss any obstacles they faced.";
        private static final String CAUSEASSESSMENT_STARTER_PROMPT = "Compose a single, very short message that the therapy coach would use to initiate the inquiry.";
        private static final String CAUSEASSESSMENT_TRIGGER_PREFIX = "Examine the following conversation and confirm that the patient has provided a reason for each one of: ";
        private static final String CAUSEASSESSMENT_TRIGGER_POSTFIX = "Ensure the reasons provided are detailed enough to understand each one of the patient's specific barriers to adherence, allowing for a tailored suggestion to improve their commitment to the therapy plan.";
        private static final String CAUSEASSESSMENT_GUARD_PREFIX = "Review the conversation and verify whether the patient has articulated a clear and extractable reason for not adhering to: ";
        private static final String CAUSEASSESSMENT_GUARD_POSTFIX = "The reason should be explicit enough to allow for an accurate GPT-based extraction and subsequent analysis.";
        private static final String CAUSEASSESSMENT_ACTION_PREFIX = "Identify and extract the specific reason the patient has given for not adhering to: ";
        private static final String CAUSEASSESSMENT_ACTION_POSTFIX = "Respond with a JSON-Object with the format {reason: reason extracted}.";
        private static final String SUMMARISE_PROMPT = "Please summarise the following conversation. Be concise, but ensure that the key points and issues are included. ";

        protected EN_DynamicCauseAssessmentState() {

        }

        public EN_DynamicCauseAssessmentState(String name, State subsequentState, Storage storage,
                        String storageKeyFrom,
                        String storageKeyTo) {
                this(name, subsequentState, storage, storageKeyFrom, storageKeyTo, true, false);
        }

        public EN_DynamicCauseAssessmentState(String name, State subsequentState, Storage storage,
                        String storageKeyFrom,
                        String storageKeyTo,
                        boolean isStarting,
                        boolean isOblivious) {
                super(EN_DynamicCauseAssessmentState.CAUSEASSESSMENT_PROMPT_PREFIX + "${" + storageKeyFrom + "} "
                                + EN_DynamicCauseAssessmentState.CAUSEASSESSMENT_PROMPT_POSTFIX,
                                name,
                                EN_DynamicCauseAssessmentState.CAUSEASSESSMENT_STARTER_PROMPT,
                                List.of(),
                                SUMMARISE_PROMPT,
                                isStarting,
                                isOblivious,
                                storage,
                                List.of(storageKeyFrom));
                Decision trigger = new DynamicDecision(
                                EN_DynamicCauseAssessmentState.CAUSEASSESSMENT_TRIGGER_PREFIX + "${" + storageKeyFrom
                                                + "}"
                                                + EN_DynamicCauseAssessmentState.CAUSEASSESSMENT_TRIGGER_POSTFIX,
                                storage, storageKeyFrom);
                Decision guard = new DynamicDecision(
                                EN_DynamicCauseAssessmentState.CAUSEASSESSMENT_GUARD_PREFIX + "${" + storageKeyFrom
                                                + "}" + EN_DynamicCauseAssessmentState.CAUSEASSESSMENT_GUARD_POSTFIX,
                                storage, storageKeyFrom);
                Action action = new DynamicExtractionAction(
                                EN_DynamicCauseAssessmentState.CAUSEASSESSMENT_ACTION_PREFIX + "${" + storageKeyFrom
                                                + "} "
                                                + CAUSEASSESSMENT_ACTION_POSTFIX,
                                storage,
                                storageKeyFrom,
                                storageKeyTo);
                Transition transition = new Transition(List.of(trigger, guard), List.of(action), subsequentState);
                this.addTransition(transition);
        }

        public void setSubsequentState(State subsequentState) {
                List<Transition> transitions = this.getTransitions();
                if (transitions.size() != 1) {
                        throw new RuntimeException("expected number of transitions to be 1 but encountered "
                                        + transitions.size());
                }
                transitions.iterator().next().setSubsequenState(subsequentState);
        }

        @Override
        protected String getPrompt() {
                Map<String, JsonElement> valuesForKeys = this.getValuesForKeys();
                if (!(valuesForKeys.values().iterator().next() instanceof JsonArray)) {
                        throw new RuntimeException(
                                        "expected storageKeyFrom being associated to a list (JasonArray) but enountered "
                                                        + valuesForKeys.values().iterator().next().getClass()
                                                        + " instead");
                }

                return NamedParametersFormatter.format(super.getPrompt(), valuesForKeys);
        }

        @Override
        public String toString() {
                return "DynamicCauseAssessmentState IS-A " + super.toString();
        }
}
