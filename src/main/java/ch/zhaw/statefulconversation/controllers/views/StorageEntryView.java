package ch.zhaw.statefulconversation.controllers.views;

public class StorageEntryView {
    private String key;
    private String value;

    public StorageEntryView(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }
}
