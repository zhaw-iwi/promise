package ch.zhaw.statefulconversation.model.commons.decisions;

import ch.zhaw.statefulconversation.model.Decision;
import jakarta.persistence.Entity;

@Entity
public class StaticDecision extends Decision {

    protected StaticDecision() {

    }

    public StaticDecision(String decisionPrompt) {
        super(decisionPrompt);
    }

    @Override
    public String toString() {
        return "StaticDecision IS-A " + super.toString();
    }
}
