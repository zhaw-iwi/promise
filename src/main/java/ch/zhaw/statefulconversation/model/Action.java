package ch.zhaw.statefulconversation.model;

import java.util.List;

import jakarta.persistence.Entity;

@Entity
public abstract class Action extends Prompt {

    protected Action() {

    }

    private String storageKeyTo;

    public Action(String actionPromp) {
        super(actionPromp);
        this.storageKeyTo = null;
    }

    public Action(String actionPrompt, Storage storage, String storageKeyTo) {
        super(actionPrompt, storage, List.of());
        this.storageKeyTo = storageKeyTo;
    }

    public Action(String actionPrompt, Storage storage, String storageKeyFrom, String storageKeyTo) {
        super(actionPrompt, storage, List.of(storageKeyFrom));
        this.storageKeyTo = storageKeyTo;
    }

    protected String getStorageKeyTo() {
        if (this.storageKeyTo == null) {
            throw new RuntimeException(
                    "this is not a dynamic action - storageKeyTo is supposed to be null");
        }
        return this.storageKeyTo;
    }

    public abstract void execute(Utterances utterances);

    @Override
    public String toString() {
        return "Action IS-A " + super.toString();
    }
}
