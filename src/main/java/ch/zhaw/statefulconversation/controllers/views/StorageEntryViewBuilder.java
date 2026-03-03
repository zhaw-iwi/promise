package ch.zhaw.statefulconversation.controllers.views;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;

public final class StorageEntryViewBuilder {
    private StorageEntryViewBuilder() {
    }

    public static List<StorageEntryView> fromStorage(Map<String, JsonElement> storage) {
        if (storage == null) {
            return List.of();
        }
        return storage.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map((entry) -> new StorageEntryView(entry.getKey(),
                        entry.getValue() == null ? "null" : entry.getValue().toString()))
                .toList();
    }
}
