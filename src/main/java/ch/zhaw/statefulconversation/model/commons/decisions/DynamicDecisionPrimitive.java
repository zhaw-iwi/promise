package ch.zhaw.statefulconversation.model.commons.decisions;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.utils.NamedParametersFormatter;
import jakarta.persistence.Entity;

@Entity
public class DynamicDecisionPrimitive extends Decision {

    protected DynamicDecisionPrimitive() {

    }

    public DynamicDecisionPrimitive(String decisionPromptTemplate, Storage storage, String storageKeyFrom) {
        super(decisionPromptTemplate, storage, storageKeyFrom);
    }

    @Override
    protected String getPrompt() {
        Map<String, JsonElement> valuesForKeys = this.getValuesForKeys();

        if (!(valuesForKeys.values().iterator().next() instanceof JsonPrimitive)) {
            throw new RuntimeException(
                    "expected storageKeyFrom " + this.getStorageKeysFrom()
                            + " being associated to a Primitive (JsonPrimitive) but enountered "
                            + valuesForKeys.values().iterator().next().getClass()
                            + valuesForKeys.values().iterator().next()
                            + " instead");
        }

        return NamedParametersFormatter.format(super.getPrompt(), valuesForKeys);
    }

    @Override
    public String toString() {
        return "DynamicDecisionPrimitive IS-A " + super.toString();
    }
}
