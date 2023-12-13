package ch.zhaw.statefulconversation.bots;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.OuterState;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@SpringBootTest
class BotTwoTransitions {

        @Autowired
        private AgentRepository repository;

        @Test
        void setUp() {

                Decision triggerAccept = new StaticDecision(
                                "return true if patient in the following conversation has accepted one of the follwing choices: schwimmen in offenem Gewässer.");
                Transition transitionAccept = new Transition(List.of(triggerAccept), List.of(), new Final());

                Decision triggerWhy = new StaticDecision(
                                "return true if the user has stated a reason for not accepting the proposed action.");
                Transition transitionWhy = new Transition(List.of(triggerWhy), List.of(), new Final());
                State askWhyState = new State(
                                "Der User hat die vorgeschlagene Massnahme nicht akzeptiert. Frage nach dem Grund weshalb.",
                                "askWhyState", "Frage weshalb", List.of(transitionWhy));
                Decision triggerDecline = new StaticDecision(
                                "return true if patient in the following conversation declined all of the follwing choices: schwimmen in offenem Gewässer");
                Transition transitionDecline = new Transition(List.of(triggerDecline), List.of(), askWhyState);

                State usersConsiderProposal = new State(
                                "Es hat sich herausgestellt dass der User nicht wie vereinbart schwimmen ging Vermittle und erkläre nun eine Massnahme aus den folgenden Massnahmen: schwimmen im offenen Gewässer und stelle sicher dass der User eine davon Massnahme akzeptiert oder ablehnt.",
                                "proposeAction",
                                "Vermittle die Massnahmen.",
                                List.of(transitionAccept, transitionDecline));

                Decision trigger = new StaticDecision("Review the chat and determine if the user wants to exit.");
                Transition transition = new Transition(List.of(trigger), List.of(), new Final());
                State outerState = new OuterState(
                                "Du bist ein erfahrener Arzt mit Spezialisierung auf chronische Erkrankungen. Du bist einfühlsam und verständnisvoll. Versuche, so kurz wie möglich zu antworten, aber stelle sicher, dass deine Antworten hilfreich und unterstützend sind.",
                                "medicalSupport",
                                List.of(transition), usersConsiderProposal);
                Agent agent = new Agent("Medical Assistant", "Medical Assistant proposing therapy options", outerState);
                agent.start();
                this.repository.save(agent);
        }
}
