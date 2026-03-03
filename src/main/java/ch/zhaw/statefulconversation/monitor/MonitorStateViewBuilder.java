package ch.zhaw.statefulconversation.monitor;

import ch.zhaw.statefulconversation.controllers.views.AgentStateInfoView;
import ch.zhaw.statefulconversation.controllers.views.AgentStateInfoViewBuilder;
import ch.zhaw.statefulconversation.controllers.views.MonitorStateView;
import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.State;

public final class MonitorStateViewBuilder {
    private MonitorStateViewBuilder() {
    }

    public static MonitorStateView fromAgent(Agent agent) {
        State currentState = agent.getCurrentState();
        AgentStateInfoView stateInfo = AgentStateInfoViewBuilder.fromState(currentState);
        return new MonitorStateView(stateInfo, agent.isActive());
    }
}
