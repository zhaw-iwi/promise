package ch.zhaw.statefulconversation.model;

import java.util.function.Supplier;

public class ObjectSerialisationSupplier implements Supplier<String> {

    private Object objectToBeSerialised;

    public ObjectSerialisationSupplier(Object objectToBeSerialised) {
        this.objectToBeSerialised = objectToBeSerialised;
    }

    @Override
    public String get() {
        return this.objectToBeSerialised.getClass() + " " + this.objectToBeSerialised.toString();
    }

}
