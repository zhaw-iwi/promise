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
public class MentalWellbeingAssessmentState extends State {

        private static final String MENTALWELLBEING_PROMPT = "Führe mit dem User ein Gespräch, um herauszufinden, wie es dem User in Bezug auf seine mentale Gesundheit geht. Frage nach unterschiedlichen Bereichen und stelle sicher, dass der User nichts mehr weiter beschäftigt. Gib keine Ratschläge und bleib beim Sammeln von möglichen Problemen. Tauche nicht zu tief in einen Bereich ein. Halte dich bei denen Antworten möglichst kurz und vermeide Fachjargon.";
        private static final String MENTALWELLBEING_STARTER_PROMPT = "Starte nun das Gespräch mit deiner ersten Frage.";
        private static final String MENTALWELLBEING_TRIGGER = "Analysiere die folgende Konversation und entscheide, ob der User keine weiteren Themen beschäftigt.";
        private static final String MENTALWELLBEING_ACTION = "Analysiere die Konversation und identifiziere spezifische Themen, die dem User möglicherweise Probleme bereiten. Beschreibe diese Themen so präzise wie möglich und erstelle ein JSON-Objekt im Format [\"Problem 1\", \"Problem 2\", ...], um die gefundenen Probleme darzustellen.";
        private static final String SUMMARISE_PROMPT = "Please summarise the following conversation. Be concise, but ensure that the key points and issues are included. ";

        protected MentalWellbeingAssessmentState() {

        }

        public MentalWellbeingAssessmentState(String name, State subsequentState, Storage storage,
                        String storageKeyTo) {
                this(name, subsequentState, storage, storageKeyTo, true, false);
        }

        public MentalWellbeingAssessmentState(String name, State subsequentState, Storage storage,
                        String storageKeyTo,
                        boolean isStarting,
                        boolean isOblivious) {
                super(MentalWellbeingAssessmentState.MENTALWELLBEING_PROMPT,
                                name,
                                MentalWellbeingAssessmentState.MENTALWELLBEING_STARTER_PROMPT,
                                List.of(),
                                SUMMARISE_PROMPT,
                                isStarting,
                                isOblivious,
                                storage,
                                List.of());
                Decision trigger = new StaticDecision(MentalWellbeingAssessmentState.MENTALWELLBEING_TRIGGER);
                Action action = new StaticExtractionAction(MentalWellbeingAssessmentState.MENTALWELLBEING_ACTION,
                                storage,
                                storageKeyTo);
                Transition transition = new Transition(List.of(trigger), List.of(action), subsequentState);
                this.addTransition(transition);
        }

        @Override
        public String toString() {
                return "MentalWellbeingAssessmentState IS-A " + super.toString();
        }
}
