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
public class DynamicActionProposalState extends State {

        private static final String ACTIONAGREEMENT_PROMPT_1 = "Es hat sich herausgestellt dass der User ";
        private static final String ACTIONAGREEMENT_PROMPT_2 = "} \nVermittle und erkläre nun eine Massnahme aus den folgenden Massnahmen ${";
        private static final String ACTIONAGREEMENT_PROMPT_3 = "} \nund stelle sicher dass der User eine davon Massnahme akzeptiert oder ablehnt.";
        private static final String ACTIONAGREEMENT_STARTER_PROMPT = "Vermittle die Massnahmen.";
        private static final String ACTIONAGREEMENT_TRIGGER = "return true if patient in the following conversation has accepted one of the follwing choices: ";
        private static final String ACTIONAGREEMENT_ACTION = "Antworte mit einem JSON-Objekt welches die getroffene Wahl als boolean True beinhaltet.";
        private static final String ACTIONREJECTION_TRIGGER = "return true if patient in the following conversation declined all of the follwing choices: ";
        private static final String ACTIONREJECTION_ACTION = "Antworte mit einem JSON-Objekt welches die getroffene Wahl als boolean False beinhaltet.";
        private static final String SUMMARISE_PROMPT = "Please summarise the following conversation. Be concise, but ensure that the key points and issues are included. ";

        public DynamicActionProposalState(String name, State subsequentState, State subsequentStateReject,
                        Storage storage,
                        String keyFindReason, String keyReason, String keyAvailableActions,
                        String storageKeyTo) {
                this(name, subsequentState, subsequentStateReject, storage, keyFindReason, keyReason,
                                keyAvailableActions, storageKeyTo, true,
                                false);
        }

        protected DynamicActionProposalState() {

        }

        public DynamicActionProposalState(String name, State subsequentState, State subsequentStateReject,
                        Storage storage, String keyFindReason,
                        String keyReason, String keyAvailableActions,
                        String storageKeyTo,
                        boolean isStarting,
                        boolean isOblivious) {
                super(DynamicActionProposalState.ACTIONAGREEMENT_PROMPT_1 + "${" + keyFindReason + "}, weil ${"
                                + keyReason + "}" + ACTIONAGREEMENT_PROMPT_2 + keyAvailableActions
                                + ACTIONAGREEMENT_PROMPT_3,
                                name,
                                DynamicActionProposalState.ACTIONAGREEMENT_STARTER_PROMPT,
                                List.of(),
                                SUMMARISE_PROMPT,
                                isStarting,
                                isOblivious,
                                storage,
                                List.of(keyFindReason, keyReason, keyAvailableActions));
                Decision trigger = new DynamicDecision(
                                DynamicActionProposalState.ACTIONAGREEMENT_TRIGGER + "${" + keyAvailableActions + "}",
                                storage, keyAvailableActions);
                Action action = new DynamicExtractionAction(
                                DynamicActionProposalState.ACTIONAGREEMENT_ACTION,
                                storage,
                                keyAvailableActions,
                                storageKeyTo);
                Transition transition = new Transition(List.of(trigger), List.of(action), subsequentState);
                this.addTransition(transition);
                Decision rejectionTrigger = new DynamicDecision(
                                DynamicActionProposalState.ACTIONREJECTION_TRIGGER + "${" + keyAvailableActions
                                                + "}",
                                storage, keyAvailableActions);
                Action rejectionAction = new DynamicExtractionAction(ACTIONREJECTION_ACTION, storage,
                                keyAvailableActions, storageKeyTo);
                Transition rejectionTransition = new Transition(List.of(rejectionTrigger), List.of(rejectionAction),
                                subsequentStateReject);
                this.addTransition(rejectionTransition);

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
                return "DynamicActionProposalState IS-A " + super.toString();
        }
}
