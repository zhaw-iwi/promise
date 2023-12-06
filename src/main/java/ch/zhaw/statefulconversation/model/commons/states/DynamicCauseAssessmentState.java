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
public class DynamicCauseAssessmentState extends State {

        private static final String CAUSEASSESSMENT_PROMPT = "Finde den Grund weshalb der Nutzer folgendes gesagt oder gemacht hat ";
        private static final String CAUSEASSESSMENT_PROMPT_2 = "Halte dich in deinen Antworten kurz und frage den User zu jedem Punkt nacheinander und nicht in einer einzigen Antwort.";
        private static final String CAUSEASSESSMENT_STARTER_PROMPT = "Starte das Gespräch um den Grund zu finden.";
        private static final String CAUSEASSESSMENT_TRIGGER = "return true if the following conversation contains a specific reason for each of the following problems: ";
        private static final String CAUSEASSESSMENT_TRIGGER_2 = " Ensure that the user provides a distinct reason for each problem. If any problem remains without a reason, please return false. For the answer to be true, the user should have addressed all the problems.";
        private static final String CAUSEASSESSMENT_GUARD = "analyse the following conversation and decide if the reason can be extracted and is different than, e.g., I didn't feel like it, or I lack motivation, or I don't know. We need to know specifically why.";
        private static final String CAUSEASSESSMENT_ACTION_1 = "extract the reason why the user did or answered the following: ";
        private static final String CAUSEASSESSMENT_ACTION_2 = "and respond with a JSON-Object with the format {reason: gefundene Antwort auf deutsch}.";
        private static final String SUMMARISE_PROMPT = "Please summarise the following conversation. Be concise, but ensure that the key points and issues are included. ";

        protected DynamicCauseAssessmentState() {

        }

        public DynamicCauseAssessmentState(String name, State subsequentState, Storage storage, String storageKeyFrom,
                        String storageKeyTo) {
                this(name, subsequentState, storage, storageKeyFrom, storageKeyTo, true, false);
        }

        public DynamicCauseAssessmentState(String name, State subsequentState, Storage storage, String storageKeyFrom,
                        String storageKeyTo,
                        boolean isStarting,
                        boolean isOblivious) {
                super(DynamicCauseAssessmentState.CAUSEASSESSMENT_PROMPT + "${" + storageKeyFrom + "} "
                                + DynamicCauseAssessmentState.CAUSEASSESSMENT_PROMPT_2,
                                name,
                                DynamicCauseAssessmentState.CAUSEASSESSMENT_STARTER_PROMPT,
                                List.of(),
                                SUMMARISE_PROMPT,
                                isStarting,
                                isOblivious,
                                storage,
                                List.of(storageKeyFrom));
                Decision trigger = new DynamicDecision(
                                DynamicCauseAssessmentState.CAUSEASSESSMENT_TRIGGER + "${" + storageKeyFrom + "}"
                                                + CAUSEASSESSMENT_TRIGGER_2,
                                storage, storageKeyFrom);
                Decision guard = new DynamicDecision(
                                DynamicCauseAssessmentState.CAUSEASSESSMENT_GUARD + "${" + storageKeyFrom + "}",
                                storage, storageKeyFrom);
                Action action = new DynamicExtractionAction(
                                DynamicCauseAssessmentState.CAUSEASSESSMENT_ACTION_1 + "${" + storageKeyFrom + "} "
                                                + CAUSEASSESSMENT_ACTION_2,
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
                                        "expected storageKeyFrom being associated to a list (JsonArray) but enountered "
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
