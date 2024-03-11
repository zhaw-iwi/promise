package ch.zhaw.statefulconversation.model.commons.actions;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Utterances;
import ch.zhaw.statefulconversation.spi.LMOpenAI;
import ch.zhaw.statefulconversation.utils.NamedParametersFormatter;
import jakarta.persistence.Entity;

@Entity
public class DynamicExtractionActionPrimitive extends Action {

    protected DynamicExtractionActionPrimitive() {

    }

    public DynamicExtractionActionPrimitive(String actionPromptTemplate, Storage storage, String storageKeyFrom,
            String storageKeyTo) {
        super(actionPromptTemplate, storage, storageKeyFrom, storageKeyTo);
    }

    @Override
    protected String getPrompt() {
        Map<String, JsonElement> valuesForKeys = this.getValuesForKeys();
        if (!(valuesForKeys.values().iterator().next() instanceof JsonPrimitive)) {
            throw new RuntimeException(
                    "expected storageKeyFrom being associated to a primtitive (JsonPrimitive) but enountered "
                            + valuesForKeys.values().iterator().next().getClass()
                            + valuesForKeys.values().iterator().next()
                            + " instead");
        }

        return NamedParametersFormatter.format(super.getPrompt(), valuesForKeys);
    }

    @Override
    public void execute(Utterances utterances) {
        JsonElement result = LMOpenAI.extract(utterances, this.getPrompt());
        this.getStorage().put(this.getStorageKeyTo(), result);
    }

    @Override
    public String toString() {
        return "DynamicExtractionActionPrimitive IS-A " + super.toString();
    }
}
