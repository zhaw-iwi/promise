package ch.zhaw.statefulconversation.model.commons.states;

import java.util.List;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.actions.StaticExtractionAction;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import jakarta.persistence.Entity;

@Entity
public class SingleChoiceState extends State {

        private static final String SINGLECHOICE_PROMPT = "Ask the user to choose one item out of the following list of items: ";
        private static final String SINGLECHOICE_STARTER_PROMPT = "Ask the user.";
        private static final String SINGLECHOICE_TRIGGER = "Examine the following chat and decide if the user indicates one choice among the following choices: ";
        private static final String SINGLECHOICE_ACTION = "Examine the following chat and extract extract the one choice the user made among the following choices: ";
        private static final String SUMMARISE_PROMPT = "Please summarise the following conversation. Be concise, but ensure that the key points and issues are included. ";

        protected SingleChoiceState() {

        }

        public SingleChoiceState(String name, List<String> choices, State subsequentState, Storage storage,
                        String storageKeyTo) {
                this(name, choices, subsequentState, storage, storageKeyTo, true, false);
        }

        public SingleChoiceState(String name, List<String> choices, State subsequentState, Storage storage,
                        String storageKeyTo,
                        boolean isStarting,
                        boolean isOblivious) {
                super(new StringBuilder(SingleChoiceState.SINGLECHOICE_PROMPT)
                                .append(String.join(", ", choices))
                                .toString(),
                                name,
                                SingleChoiceState.SINGLECHOICE_STARTER_PROMPT,
                                List.of(),
                                SUMMARISE_PROMPT,
                                isStarting,
                                isOblivious,
                                storage,
                                List.of());
                Decision trigger = new StaticDecision(
                                new StringBuilder(SingleChoiceState.SINGLECHOICE_TRIGGER)
                                                .append(String.join(", ", choices))
                                                .toString());
                Action action = new StaticExtractionAction(
                                new StringBuilder(SingleChoiceState.SINGLECHOICE_ACTION)
                                                .append(String.join(", ", choices))
                                                .toString(),
                                storage,
                                storageKeyTo);
                Transition transition = new Transition(List.of(trigger), List.of(action), subsequentState);
                this.addTransition(transition);
        }

        @Override
        public String toString() {
                return "SingleChoiceState IS-A " + super.toString();
        }
}
