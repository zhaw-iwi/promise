package ch.zhaw.statefulconversation.model;

import java.util.List;

import jakarta.persistence.Entity;

@Entity
public abstract class Decision extends Prompt {

    protected Decision() {

    }

    public Decision(String decisionPrompt) {
        super(decisionPrompt);
    }

    public Decision(String decisionPrompt, Storage storage, String storageKeysFrom) {
        super(decisionPrompt, storage, List.of(storageKeysFrom));
    }

    @Override
    public String toString() {
        return "Decision IS-A " + super.toString();
    }
}
