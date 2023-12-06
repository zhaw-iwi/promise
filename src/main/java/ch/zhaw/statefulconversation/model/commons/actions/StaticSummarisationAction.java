package ch.zhaw.statefulconversation.model.commons.actions;

import com.google.gson.JsonElement;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Utterances;
import ch.zhaw.statefulconversation.spi.LMOpenAI;
import jakarta.persistence.Entity;

@Entity
public class StaticSummarisationAction extends Action {

    protected StaticSummarisationAction() {

    }

    public StaticSummarisationAction(String actionPrompt, Storage storage, String storageKeyTo) {
        super(actionPrompt, storage, storageKeyTo);
    }

    @Override
    public void execute(Utterances utterances) {
        JsonElement result = LMOpenAI.extract(utterances, this.getPrompt());
        this.getStorage().put(this.getStorageKeyTo(), result);
    }

    @Override
    public String toString() {
        return "StaticSummarisationAction IS-A " + super.toString();
    }
}
