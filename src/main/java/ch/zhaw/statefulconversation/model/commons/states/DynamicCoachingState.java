package ch.zhaw.statefulconversation.model.commons.states;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.decisions.DynamicDecision;
import ch.zhaw.statefulconversation.utils.NamedParametersFormatter;
import jakarta.persistence.Entity;

@Entity
public class DynamicCoachingState extends State {

        private static final String COACHING_PROMPT = "Der Benutzer hat das Thema ";
        private static final String COACHING_PROMPT_2 = " angesprochen. Es ist wichtig, sensibel auf solche Themen einzugehen. Du könntest beginnen, indem du dem Benutzer einfühlsam antwortest und Unterstützung anbietest. Zum Beispiel: Ich verstehe, dass ";
        private static final String COACHING_PROMPT_3 = " eine herausfordernde Angelegenheit sein kann. Es ist mutig, darüber zu sprechen. Falls der Benutzer nach Ratschlägen oder Tipps fragt, kannst du sanfte und allgemeine Ratschläge anbieten, die auf ";
        private static final String COACHING_PROMPT_4 = " zutreffen könnten. Zum Beispiel: Wenn du möchtest, kann ich einige allgemeine Tipps zur Bewältigung von ";
        private static final String COACHING_PROMPT_5 = " teilen. Denk daran, dass ich hier bin, um zuzuhören und zu unterstützen. Bitte stelle sicher, dass deine Antwort einfühlsam und unterstützend ist und dass du Ratschläge mit Vorsicht gibst, da jeder Fall einzigartig ist und individuelle Bedürfnisse berücksichtigt werden sollten.";
        private static final String COACHING_STARTER_PROMPT = "Starte indem du das erste Thema sensibel ansprichst.";
        private static final String COACHING_TRIGGER = "Examine the following chat and decide if the assistant has given userful adive for the user and the user is ready to move on: ";
        private static final String SUMMARISE_PROMPT = "Please summarise the following conversation. Be concise, but ensure that the key points and issues are included. ";

        protected DynamicCoachingState() {

        }

        public DynamicCoachingState(String name, State subsequentState, Storage storage, String storageKeyFrom) {
                this(name, subsequentState, storage, storageKeyFrom, true, false);
        }

        public DynamicCoachingState(String name, State subsequentState, Storage storage, String storageKeyFrom,
                        boolean isStarting,
                        boolean isOblivious) {
                super(DynamicCoachingState.COACHING_PROMPT + "${" + storageKeyFrom + "}" + COACHING_PROMPT_2 + "${"
                                + storageKeyFrom + "}" + COACHING_PROMPT_2 + "${" + storageKeyFrom + "}"
                                + COACHING_PROMPT_3 + "${" + storageKeyFrom + "}" + COACHING_PROMPT_4 + "${"
                                + storageKeyFrom + "}" + COACHING_PROMPT_5,
                                name,
                                DynamicCoachingState.COACHING_STARTER_PROMPT,
                                List.of(),
                                SUMMARISE_PROMPT,
                                isStarting,
                                isOblivious,
                                storage,
                                List.of(storageKeyFrom));
                Decision trigger = new DynamicDecision(
                                DynamicCoachingState.COACHING_TRIGGER + "${" + storageKeyFrom + "}",
                                storage, storageKeyFrom);
                Transition transition = new Transition(List.of(trigger), List.of(), subsequentState);
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
                return "DynamicCoachingState IS-A " + super.toString();
        }
}
