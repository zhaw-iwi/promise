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
public class DynamicGatherState extends State {

        private static final String GATHER_PROMPT = "Ask the user to provide one value for each of the following slots: ";
        private static final String GATHER_STARTER_PROMPT = "Ask the user.";
        private static final String GATHER_TRIGGER = "Examine the following chat and decide if the user provides all values for the following slots: ";
        private static final String GATHER_ACTION = "Examine the following chat and extract each value for all of the following slots: ";
        private static final String SUMMARISE_PROMPT = "Please summarise the following conversation. Be concise, but ensure that the key points and issues are included. ";

        protected DynamicGatherState() {

        }

        public DynamicGatherState(String name, State subsequentState, Storage storage, String storageKeyFrom,
                        String storageKeyTo) {
                this(name, subsequentState, storage, storageKeyFrom, storageKeyTo, true, false);
        }

        public DynamicGatherState(String name, State subsequentState, Storage storage, String storageKeyFrom,
                        String storageKeyTo,
                        boolean isStarting,
                        boolean isOblivious) {
                super(DynamicGatherState.GATHER_PROMPT + "${" + storageKeyFrom + "}",
                                name,
                                DynamicGatherState.GATHER_STARTER_PROMPT,
                                List.of(),
                                SUMMARISE_PROMPT,
                                isStarting,
                                isOblivious,
                                storage,
                                List.of(storageKeyFrom));
                Decision trigger = new DynamicDecision(DynamicGatherState.GATHER_TRIGGER + "${" + storageKeyFrom + "}",
                                storage, storageKeyFrom);
                Action action = new DynamicExtractionAction(
                                DynamicGatherState.GATHER_ACTION + "${" + storageKeyFrom + "}",
                                storage,
                                storageKeyFrom,
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
                return "DynamicGatherState IS-A " + super.toString();
        }
}
