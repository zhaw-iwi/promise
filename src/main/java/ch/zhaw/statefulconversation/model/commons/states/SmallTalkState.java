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
public class SmallTalkState extends State {

        private static final String SMALLTALK_PROMPT = "Führe Small Talk mit dem Benutzer. Begrüße ihn herzlich und frage, ob er geduzt oder gesiezt werden möchte. Erkundige dich, wie es ihm geht. Stelle immer nur eine Frage auf einmal und gehe auf seine Antworten ein, um eine angenehme Konversation aufzubauen und Nähe zu schaffen.";
        private static final String SMALLTALK_STARTER_PROMPT = "Starte nun das Gespräch mit deiner ersten Frage.";
        private static final String SMALLTALK_TRIGGER = "Analysiere die folgende Konversation und entscheide folgendes: \n - der User wurde gefragt, ob er geduzt oder gesiezt werden will \n - Der User wurde gefragt wie es ihm geht.\n Beide Bedingungen müssen erfüllt werden.";
        private static final String SMALLTALK_ACTION = "Extrahiere die Besprochenen Themen aus der geführten Konversation und erzeuge ein JSON objekt im Format {\"Thema1\": \"Zusammenfassung\", \"Thema2\": \"Zusammenfassung\", ...}";
        private static final String SUMMARISE_PROMPT = "Please summarise the following conversation. Be concise, but ensure that the key points and issues are included. ";

        protected SmallTalkState() {

        }

        public SmallTalkState(String name, State subsequentState, Storage storage,
                        String storageKeyTo) {
                this(name, subsequentState, storage, storageKeyTo, true, false);
        }

        public SmallTalkState(String name, State subsequentState, Storage storage,
                        String storageKeyTo,
                        boolean isStarting,
                        boolean isOblivious) {
                super(SmallTalkState.SMALLTALK_PROMPT,
                                name,
                                SmallTalkState.SMALLTALK_STARTER_PROMPT,
                                List.of(),
                                SUMMARISE_PROMPT,
                                isStarting,
                                isOblivious,
                                storage,
                                List.of());
                Decision trigger = new StaticDecision(SmallTalkState.SMALLTALK_TRIGGER);
                Action action = new StaticExtractionAction(SmallTalkState.SMALLTALK_ACTION,
                                storage,
                                storageKeyTo);
                Transition transition = new Transition(List.of(trigger), List.of(action), subsequentState);
                this.addTransition(transition);
        }

        @Override
        public String toString() {
                return "SmallTalkState IS-A " + super.toString();
        }
}
