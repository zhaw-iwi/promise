package ch.zhaw.statefulconversation.bots;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.Gson;

import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.commons.states.DynamicSingleChoiceStateShrinking;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@SpringBootTest
class HearingLossAssistantBotDebug {

        private static final Gson GSON = new Gson();

        @Autowired
        private AgentRepository repository;

        @Test
        void setUp() {
                Storage storage = new Storage();
                storage.put("potentialIssues",
                                HearingLossAssistantBotDebug.GSON
                                                .toJsonTree(List.of("Müdigkeit bei der Arbeit",
                                                                "Nebenwirkungen der Coronaimpfung")));
                State chooseTopicState = new DynamicSingleChoiceStateShrinking("chooseTopic", new Final(),
                                storage,
                                "potentialIssues", "chosenTopic", true, true);
                Agent agent = new Agent("Hearingloss Assistant",
                                "Providing mentalwellbeing coaching for young adults with a hearing impairment.",
                                chooseTopicState,
                                storage);
                agent.start();
                this.repository.save(agent);
        }
}
