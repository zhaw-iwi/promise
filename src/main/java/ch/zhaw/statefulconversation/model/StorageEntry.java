package ch.zhaw.statefulconversation.model;

import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class StorageEntry {

    @Id
    @GeneratedValue
    private UUID id;

    protected StorageEntry() {

    }

    private String entryKey;
    @Column(name = "entry_value", length = 1000)
    @Convert(converter = JsonObjectConverter.class)
    private JsonElement entryValue;

    public StorageEntry(String key, JsonElement value) {
        this.entryKey = key;
        this.entryValue = value;
    }

    public String getKey() {
        return this.entryKey;
    }

    public JsonElement getValue() {
        return this.entryValue;
    }

    public void setValue(JsonElement value) {
        this.entryValue = value;
    }

    @Converter
    private static class JsonObjectConverter implements AttributeConverter<Object, String> {
        private static final Gson GSON = new Gson();

        @Override
        public String convertToDatabaseColumn(Object memoryRepresentation) {
            return memoryRepresentation.toString();
        }

        @Override
        public Object convertToEntityAttribute(String databaseRepresentation) {
            return JsonObjectConverter.GSON.fromJson(databaseRepresentation, JsonElement.class);
        }
    }
}
