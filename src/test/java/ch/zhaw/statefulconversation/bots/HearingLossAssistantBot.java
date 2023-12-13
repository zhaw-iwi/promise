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
import ch.zhaw.statefulconversation.model.commons.states.DynamicCauseAssessmentState;
import ch.zhaw.statefulconversation.model.commons.states.DynamicSingleChoiceStateShrinking;
import ch.zhaw.statefulconversation.model.commons.states.MentalWellbeingAssessmentState;
import ch.zhaw.statefulconversation.model.commons.states.SmallTalkState;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@SpringBootTest
class HearingLossAssistantBot {

        private static final Gson GSON = new Gson();

        @Autowired
        private AgentRepository repository;

        @Test
        void setUp() {
                Storage storage = new Storage();
                storage.put("potentialIssues",
                                HearingLossAssistantBot.GSON
                                                .toJsonTree(List.of("Müdigkeit bei der Arbeit.",
                                                                "Nebenwirkungen der Coronaimpfung")));
                DynamicCauseAssessmentState findCauseForIssue = new DynamicCauseAssessmentState("findReasonForIssue",
                                null,
                                storage,
                                "chosenTopic", "reasonForIssue", true, false);
                State chooseTopicState = new DynamicSingleChoiceStateShrinking("chooseTopic", findCauseForIssue,
                                storage,
                                "potentialIssues", "chosenTopic", true, true);
                findCauseForIssue.setSubsequentState(chooseTopicState);
                State assessMentalwellbeing = new MentalWellbeingAssessmentState("UsersAgreenOnAction",
                                chooseTopicState,
                                storage, "potentialIssues");
                State smalltalk = new SmallTalkState("smalltalk",
                                assessMentalwellbeing,
                                storage, "discussed_topics");
                Decision trigger = new StaticDecision("Review the chat and determine if the user wants to exit.");
                Transition transition = new Transition(List.of(trigger), List.of(), new Final());
                OuterState HearingLossAssistant = new OuterState(
                                "Du bist ein Psychologe mit Fachwissen im Umgang mit gehörlosen Jugendlichen. Du bist vertraut mit den alltäglichen Herausforderungen, denen gehörbeeinträchtigte Menschen gegenüberstehen, und verstehst, wie diese sich auf ihre sozialen Beziehungen auswirken können.",
                                "HearinglossAssistant",
                                List.of(transition), chooseTopicState);
                Agent agent = new Agent("Hearingloss Assistant",
                                "Providing mentalwellbeing coaching for young adults with a hearing impairment.",
                                HearingLossAssistant,
                                storage);
                agent.start();
                this.repository.save(agent);
        }
}
