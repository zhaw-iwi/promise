package ch.zhaw.statefulconversation.controllers;

import java.util.List;

import ch.zhaw.statefulconversation.controllers.dto.SingleStateAgentCreateDTO;
import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.actions.StaticExtractionAction;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;

public class AgentMetaUtility {

        public static Agent createSingleStateAgent(SingleStateAgentCreateDTO data) {
                var storage = new Storage();

                Decision trigger = new StaticDecision(data.getTriggerToFinalPrompt());
                Decision guard = new StaticDecision(data.getGuardToFinalPrompt());
                Action action = new StaticExtractionAction(data.getActionToFinalPrompt(), storage, "summary");
                Transition transition = new Transition(List.of(trigger, guard), List.of(action),
                                new Final("User Exit Final"));

                State state = new State(data.getStatePrompt(), data.getStateName(), data.getStateStarterPrompt(),
                                List.of(transition));

                Agent result = new Agent(data.getAgentName(), data.getAgentDescription(), state, storage);
                result.start();

                return result;
        }
}
