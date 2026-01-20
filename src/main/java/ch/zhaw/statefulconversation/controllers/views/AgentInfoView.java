package ch.zhaw.statefulconversation.controllers.views;

import java.util.UUID;

public class AgentInfoView {
    private UUID id;
    private String name;
    private String description;
    private boolean isActive;

    public AgentInfoView(UUID id, String name, String descripion, boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = descripion;
        this.isActive = isActive;
    }

    public UUID getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isActive() {
        return this.isActive;
    }
}
