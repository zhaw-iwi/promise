package ch.zhaw.statefulconversation.bots;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.Gson;

import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.OuterState;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.model.commons.states.DynamicActionAgreementState;
import ch.zhaw.statefulconversation.model.commons.states.DynamicCauseAssessmentState;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@SpringBootTest
class MedicalAssistantBot {

        private static final Gson GSON = new Gson();

        @Autowired
        private AgentRepository repository;

        @Test
        void setUp() {
                Storage storage = new Storage();
                storage.put("missedAgreement",
                                MedicalAssistantBot.GSON.toJsonTree(List.of("Patient ist nicht schwimmen gegangen.")));

                storage.put("availableActions",
                                GSON.toJsonTree(List.of("Aquafit in der Gruppe", "Schwimmen in offenenem Gewässer")));

                State usersAgreeOnAction = new DynamicActionAgreementState("UsersAgreenOnAction", new Final(),
                                storage,
                                "missedAgreement", "reason",
                                "availableActions", "chosenAction");
                State usersSayReason = new DynamicCauseAssessmentState("UsersSayReason",
                                usersAgreeOnAction,
                                storage, "missedAgreement", "reason");
                Decision trigger = new StaticDecision("Review the chat and determine if the user wants to exit.");
                Transition transition = new Transition(List.of(trigger), List.of(), new Final());
                OuterState medicalAssistant = new OuterState(
                                "Du bist ein erfahrener Arzt mit Spezialisierung auf chronische Erkrankungen. Du bist einfühlsam und verständnisvoll. Versuche, so kurz wie möglich zu antworten, aber stelle sicher, dass deine Antworten hilfreich und unterstützend sind.",
                                "medicalSupport",
                                List.of(transition), usersSayReason);
                Agent agent = new Agent("Medical Assistant", "Providing medical assistance.", medicalAssistant,
                                storage);
                agent.start();
                this.repository.save(agent);
        }
}
