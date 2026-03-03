package ch.zhaw.statefulconversation.controllers.views;

import java.util.List;

import ch.zhaw.statefulconversation.model.OuterState;
import ch.zhaw.statefulconversation.model.State;

public final class AgentStateInfoViewBuilder {
    private AgentStateInfoViewBuilder() {
    }

    public static AgentStateInfoView fromState(State currentState) {
        String stateName = currentState.getName();
        String innerName = null;
        List<String> innerNames = List.of();
        if (currentState instanceof OuterState outerState && outerState.getInnerCurrent() != null) {
            innerName = outerState.getInnerCurrent().getName();
            innerNames = outerState.getInnerCurrentChain();
        }
        return new AgentStateInfoView(stateName, innerName, innerNames);
    }
}
