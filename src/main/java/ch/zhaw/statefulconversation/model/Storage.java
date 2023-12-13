package ch.zhaw.statefulconversation.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Storage {

    @Id
    @GeneratedValue
    private UUID id;

    public UUID getID() {
        return this.id;
    }

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<StorageEntry> entries;

    public Storage() {
        this.entries = new ArrayList<StorageEntry>();
    }

    public void put(String key, JsonElement value) {
        for (StorageEntry current : this.entries) {
            if (current.getKey().equals(key)) {
                current.setValue(value);
                return;
            }
        }

        StorageEntry entry = new StorageEntry(key, value);
        this.entries.add(entry);
    }

    public boolean containsKey(String key) {
        for (StorageEntry current : this.entries) {
            if (current.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public JsonElement get(String key) {
        for (StorageEntry candiate : this.entries) {
            if (candiate.getKey().equals(key)) {
                return candiate.getValue();
            }
        }
        throw new RuntimeException("this storage does not contain an entry with key = " + key);
    }

    public StorageEntry remove(String key) {
        for (StorageEntry current : this.entries) {
            if (current.getKey().equals(key)) {
                this.entries.remove(current);
                return current;
            }
        }
        throw new RuntimeException("this storage does not contain an entry with key = " + key);
    }

    public Map<String, JsonElement> toMap() {
        Map<String, JsonElement> result = new HashMap<>();
        for (StorageEntry current : this.entries) {
            result.put(current.getKey(), current.getValue());
        }
        return result;
    }

    @Override
    public String toString() {
        return "Storage containing " + this.entries;
    }

    // @TODO utility serialisation/deserialisation methods
    public static List<String> toListOfString(JsonElement jsonListOfStrings) {
        List<JsonElement> listOfJsonElements = jsonListOfStrings.getAsJsonArray().asList();
        List<String> result = new ArrayList<String>();
        for (JsonElement current : listOfJsonElements) {
            result.add(current.getAsString());
        }
        return result;
    }

    private static Gson GSON = new Gson();

    public static JsonElement toJsonElement(Object javaObject) {
        return Storage.GSON.toJsonTree(javaObject);
    }

}
