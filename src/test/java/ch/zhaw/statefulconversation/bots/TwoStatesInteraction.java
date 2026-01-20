package ch.zhaw.statefulconversation.bots;

import java.util.List;

import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.OuterState;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.actions.TransferUtterancesAction;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@SpringBootTest
class TwoStatesInteraction {

        // Outer state
        private static final String PROMPT_OUTERSTATE = """
                        Du bist ein empathischer aber klar strukturierter Coach für Verhaltensänderungen.
                        Du arbeitest lösungsorientiert mit dem Health Action Process Approach HAPA nach Schwarzer.
                        Sprich den Nutzer motivierend und respektvoll an. Verwende kurze klare Sätze. Maximal zwei Sätze pro Nachricht.
                        Stelle gezielte Fragen und fasse regelmäßig zusammen. Bleibe professionell und freundlich.
                        Keine Therapie und keine pathologischen Themen. Verweise freundlich auf professionelle Hilfe falls nötig.
                        """;

        private static final String PROMPT_OUTERSTATE_TRIGGER = """
                        Analysiere die letzten Nutzeräußerungen.
                        Entscheide ob die Person die Unterhaltung beenden oder pausieren möchte.
                        Achte auf Formulierungen wie stop abbrechen später genug danke passt für heute oder ähnliche Hinweise.
                        Antworte mit true wenn klar ist dass die Unterhaltung beendet oder pausiert werden soll sonst mit false.
                        """;

        // Inner state one: SMART-Ziel entwickeln
        private static final String PROMPT_INNERSTATE_ONE = """
                        Führe den Nutzer Schritt für Schritt zu einem konkreten SMART Ziel.
                        Orientiere dich am Health Action Process Approach HAPA.
                        Leite gezielt durch Motivation Wunsch Bedingungen und Hindernisse.
                        Hilf beim Formulieren eines Ziels das spezifisch messbar attraktiv realistisch und terminiert ist.
                        Wenn der Nutzer mehrere Themen nennt hilf ihm eine Priorität zu wählen und konzentriere dich auf ein Ziel.
                        Erarbeite das SMART Ziel ausschließlich zu dieser priorisierten Gewohnheit.
                        Verwende alltagsnahe Beispiele wie Schlaf Bewegung Ernährung oder Bildschirmzeit.
                        Vermeide pathologische Themen und verweise freundlich auf Hilfe falls nötig.
                        Fasse am Ende das SMART Ziel klar zusammen und bitte den Nutzer es zu bestätigen.
                        """;

        private static final String PROMPT_INNERSTATE_ONE_STARTER = """
                        Erzeuge eine kurze erste Nachricht um diese Phase zu eröffnen.
                        Begrüße den Nutzer freundlich stelle dich als Coach vor und frage nach dem aktuellen Veränderungswunsch.
                        """;

        private static final String PROMPT_INNERSTATE_ONE_TRIGGER = """
                        Analysiere die Unterhaltung und entscheide ob der Nutzer ein SMART Ziel formuliert und bestätigt hat.
                        Ein SMART Ziel ist spezifisch messbar attraktiv realistisch und terminiert.
                        Antworte mit true wenn alle Kriterien klar erfüllt und vom Nutzer bestätigt sind.
                        Antworte mit false wenn das Ziel noch unklar ist oder die Bestätigung fehlt.
                        """;

        // Inner state two: Umsetzungsplan entwickeln
        private static final String PROMPT_INNERSTATE_TWO = """
                        Hilf dem Nutzer einen konkreten Plan zur Umsetzung seines SMART Ziels zu entwickeln.
                        Orientiere dich am Health Action Process Approach und Prinzipien aus Atomic Habits.
                        Stelle gezielte Fragen zu allen W Fragen Was Wann Wo Wie Mit wem Womit Wofür.
                        Führe den Nutzer dazu Hilfen und Hilfsmittel zu wählen um das Verhalten dauerhaft beizubehalten.
                        Verwende Prinzipien
                        Make it Obvious klare Auslöser und Umgebungsgestaltung
                        Make it Attractive Handlung mit etwas Angenehmem verknüpfen
                        Make it Easy Einstiegshürden senken kleine Schritte
                        Make it Satisfying Fortschritt sichtbar machen Belohnung
                        Gehe strukturiert durch mögliche Hindernisse und passende Lösungsstrategien.
                        Nutze Formulierungen wie Implementation Intentions und Habit Stacking.
                        Fasse am Ende den Plan klar zusammen und bestärke den Nutzer.
                        """;

        private static final String PROMPT_INNERSTATE_TWO_STARTER = """
                        Erzeuge eine kurze Nachricht um diese Phase zu starten.
                        Stelle eine Überleitung vom SMART Ziel zur praktischen Umsetzung her.
                        Frage was der Nutzer konkret als Nächstes tun möchte um das Ziel zu erreichen.
                        """;

        private static final String PROMPT_INNERSTATE_TWO_TRIGGER = """
                        Analysiere die Unterhaltung und entscheide ob der Nutzer einen konkreten Umsetzungsplan formuliert hat und dieser zusammengefasst sowie bestätigt wurde.
                        Antworte mit true wenn der Plan klar vollständig und vom Nutzer bestätigt ist.
                        Antworte mit false wenn noch Punkte fehlen oder keine Bestätigung vorliegt.
                        """;

        // Final inner summary
        private static final String PROMPT_INNERSTATE_FINAL = """
                        Fasse kurz zusammen welches SMART Ziel und welcher Umsetzungsplan formuliert wurde.
                        Betone Selbstwirksamkeit und positive Veränderung.
                        Schließe das Gespräch mit einer motivierenden und wertschätzenden Botschaft ab.
                        Maximal drei kurze Sätze.
                        """;

        @Autowired
        private AgentRepository repository;

        @RepeatedTest(12)
        void setUp() {

                State innerFinal = new Final("Regular Ending Final", TwoStatesInteraction.PROMPT_INNERSTATE_FINAL);

                Transition innerTwoToFinal = new Transition(
                                new StaticDecision(TwoStatesInteraction.PROMPT_INNERSTATE_TWO_TRIGGER),
                                new TransferUtterancesAction(innerFinal),
                                innerFinal);
                State innerStateTwo = new State(
                                TwoStatesInteraction.PROMPT_INNERSTATE_TWO,
                                "Umsetzungsplan entwickeln",
                                TwoStatesInteraction.PROMPT_INNERSTATE_TWO_STARTER,
                                List.of(innerTwoToFinal));

                Transition innerOneToTwo = new Transition(
                                new StaticDecision(TwoStatesInteraction.PROMPT_INNERSTATE_ONE_TRIGGER),
                                new TransferUtterancesAction(innerStateTwo),
                                innerStateTwo);
                State innerStateOne = new State(
                                TwoStatesInteraction.PROMPT_INNERSTATE_ONE,
                                "SMART Ziel entwickeln",
                                TwoStatesInteraction.PROMPT_INNERSTATE_ONE_STARTER,
                                List.of(innerOneToTwo));

                State outerFinal = new Final("User Exit Final");
                Transition outerToFinal = new Transition(
                                new StaticDecision(TwoStatesInteraction.PROMPT_OUTERSTATE_TRIGGER),
                                new TransferUtterancesAction(outerFinal),
                                outerFinal);
                State outerState = new OuterState(
                                TwoStatesInteraction.PROMPT_OUTERSTATE,
                                "Globaler Rahmen und Abbrucherkennung",
                                List.of(outerToFinal),
                                innerStateOne);

                Agent agent = new Agent(
                                "Gewohnheitscoach",
                                "Phase eins SMART Ziel entwickeln. Phase zwei konkreten Umsetzungsplan gestalten nach HAPA und Atomic Habits. Abschluss mit motivierender Zusammenfassung.",
                                outerState);
                agent.start();
                this.repository.save(agent);
        }
}
