package ch.zhaw.statefulconversation.controllers.dto;

/*
{
    "type": 0,
    "agentName": "Digital Companion",
    "agentDescription": "Daily check-in conversation.",
    "stateName": "Check-In Interaction",
    "statePrompt": "As a digital therapy coach, conduct daily check-ins with open-ended, empathetic questions to assess the patient’s well-being. Listen, encourage elaboration, affirm achievements, and support without guiding or suggesting changes. Keep responses brief (1–2 sentences) and end with a summary and kind farewell.",
    "stateStarterPrompt": "Compose a single, very short message that the therapy coach would use to initiate today's check-in conversation.",
    "triggerToFinalPrompt": "Review the patient’s latest messages for any cues suggesting they want to pause or end the conversation, like requests for a break or indications of needing time.",
    "guardToFinalPrompt": "Review the conversation to confirm the patient hasn't mentioned any unresolved physical or mental discomfort.",
    "actionToFinalPrompt": "Summarize the coach-patient conversation, focusing on therapy adherence, the patient’s attitude, and well-being for physician review."
}
 */

public class SingleStateAgentCreateDTO {
    private int type;
    private String agentName;
    private String agentDescription;
    private String stateName;
    private String statePrompt;
    private String stateStarterPrompt;
    private String triggerToFinalPrompt;
    private String guardToFinalPrompt;
    private String actionToFinalPrompt;

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAgentName() {
        return this.agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentDescription() {
        return this.agentDescription;
    }

    public void setAgentDescription(String agentDescription) {
        this.agentDescription = agentDescription;
    }

    public String getStateName() {
        return this.stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getStatePrompt() {
        return this.statePrompt;
    }

    public void setStatePrompt(String statePrompt) {
        this.statePrompt = statePrompt;
    }

    public String getStateStarterPrompt() {
        return this.stateStarterPrompt;
    }

    public void setStateStarterPrompt(String stateStarterPrompt) {
        this.stateStarterPrompt = stateStarterPrompt;
    }

    public String getTriggerToFinalPrompt() {
        return this.triggerToFinalPrompt;
    }

    public void setTriggerToFinalPrompt(String triggerToFinalPrompt) {
        this.triggerToFinalPrompt = triggerToFinalPrompt;
    }

    public String getGuardToFinalPrompt() {
        return this.guardToFinalPrompt;
    }

    public void setGuardToFinalPrompt(String guardToFinalPrompt) {
        this.guardToFinalPrompt = guardToFinalPrompt;
    }

    public String getActionToFinalPrompt() {
        return this.actionToFinalPrompt;
    }

    public void setActionToFinalPrompt(String actionToFinalPrompt) {
        this.actionToFinalPrompt = actionToFinalPrompt;
    }

}
