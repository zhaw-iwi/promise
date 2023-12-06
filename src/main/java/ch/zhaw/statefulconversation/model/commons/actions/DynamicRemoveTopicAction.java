package ch.zhaw.statefulconversation.model.commons.actions;

import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Utterances;
import ch.zhaw.statefulconversation.utils.NamedParametersFormatter;
import jakarta.persistence.Entity;

@Entity
public class DynamicRemoveTopicAction extends Action {

    protected DynamicRemoveTopicAction() {

    }

    public DynamicRemoveTopicAction(String actionPromptTemplate, Storage storage, String storageKeyFrom,
            String storageKeyTo) {
        super(actionPromptTemplate, storage, storageKeyFrom, storageKeyTo);
    }

    @Override
    protected String getPrompt() {
        Map<String, JsonElement> valuesForKeys = this.getValuesForKeys();
        if (!(valuesForKeys.values().iterator().next() instanceof JsonArray)) {
            throw new RuntimeException(
                    "expected storageKeyFrom being associated to a list (JsonArray) but enountered "
                            + valuesForKeys.values().iterator().next().getClass()
                            + " instead");
        }

        return NamedParametersFormatter.format(super.getPrompt(), valuesForKeys);
    }

    @Override
    public void execute(Utterances utterances) {
        // Get the JSON array from storage
        JsonElement topicsTo = this.getStorage().get(this.getStorageKeysFrom().get(0));
        JsonElement topicFrom = this.getStorage().get(this.getStorageKeyTo());

        if (topicsTo instanceof JsonArray && topicFrom instanceof JsonPrimitive) {
            JsonArray topicsArray = topicsTo.getAsJsonArray();
            String stringToRemove = topicFrom.getAsString();

            // Check each element in the JSON array
            for (int i = 0; i < topicsArray.size(); i++) {
                JsonElement element = topicsArray.get(i);

                // Check if the element is a JSON primitive and matches the string to remove
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
                        && element.getAsString().equals(stringToRemove)) {

                    // Remove the matching element
                    topicsArray.remove(i);
                    break; // Exit the loop after removal
                }
            }

            // Put the modified JSON array back into storage with the specified key -->
            // should work without this
            // this.getStorage().put(this.getStorageKeyTo(), topicsArray);
        } else {
            throw new RuntimeException("Invalid data in storage.");
        }
    }

    @Override
    public String toString() {
        return "DynamicRemoveTopicAction IS-A " + super.toString();
    }
}
