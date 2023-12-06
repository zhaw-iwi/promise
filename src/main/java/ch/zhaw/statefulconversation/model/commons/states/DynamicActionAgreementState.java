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
public class DynamicActionAgreementState extends State {

        private static final String ACTIONAGREEMENT_PROMPT_1 = "Es hat sich herausgestellt dass der User ";
        private static final String ACTIONAGREEMENT_PROMPT_2 = "} \nVermittle nun eine Massnahme aus den folgenden Massnahmen ${";
        private static final String ACTIONAGREEMENT_PROMPT_3 = "} \nund stelle sicher dass der User eine davon Massnahme akzeptiert.";
        private static final String ACTIONAGREEMENT_STARTER_PROMPT = "Vermittle die Massnahmen.";
        private static final String ACTIONAGREEMENT_TRIGGER = "return true if patient in the following conversation has accepted one of the follwing choices: ";
        private static final String ACTIONAGREEMENT_ACTION = "Antworte mit einem JSON-Objekt welches die getroffene Wahl beinhaltet im Format {\"Wahl\": \"die akzeptierte Massnahme\"}.";
        private static final String SUMMARISE_PROMPT = "Please summarise the following conversation. Be concise, but ensure that the key points and issues are included. ";

        public DynamicActionAgreementState(String name, State subsequentState, Storage storage,
                        String keyFindReason, String keyReason, String keyAvailableActions,
                        String storageKeyTo) {
                this(name, subsequentState, storage, keyFindReason, keyReason, keyAvailableActions, storageKeyTo, true,
                                false);
        }

        protected DynamicActionAgreementState() {

        }

        public DynamicActionAgreementState(String name, State subsequentState, Storage storage, String keyFindReason,
                        String keyReason, String keyAvailableActions,
                        String storageKeyTo,
                        boolean isStarting,
                        boolean isOblivious) {
                super(DynamicActionAgreementState.ACTIONAGREEMENT_PROMPT_1 + "${" + keyFindReason + "}, weil ${"
                                + keyReason + "}" + ACTIONAGREEMENT_PROMPT_2 + keyAvailableActions
                                + ACTIONAGREEMENT_PROMPT_3,
                                name,
                                DynamicActionAgreementState.ACTIONAGREEMENT_STARTER_PROMPT,
                                List.of(),
                                SUMMARISE_PROMPT,
                                isStarting,
                                isOblivious,
                                storage,
                                List.of(keyFindReason, keyReason, keyAvailableActions));
                Decision trigger = new DynamicDecision(
                                DynamicActionAgreementState.ACTIONAGREEMENT_TRIGGER + "${" + keyAvailableActions + "}",
                                storage, keyAvailableActions);
                Action action = new DynamicExtractionAction(
                                DynamicActionAgreementState.ACTIONAGREEMENT_ACTION,
                                storage,
                                keyAvailableActions,
                                storageKeyTo);
                Transition transition = new Transition(List.of(trigger), List.of(action), subsequentState);
                this.addTransition(transition);
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
                return "DynamicActionAgreementState IS-A " + super.toString();
        }
}
