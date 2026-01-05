package ch.zhaw.statefulconversation.controllers;

public enum AgentMetaType {
    singleState(0),
    aurestaurant(1);

    private final int value;

    AgentMetaType(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}