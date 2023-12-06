package ch.zhaw.statefulconversation.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.StorageEntry;
import ch.zhaw.statefulconversation.repositories.StorageRepository;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
public class StoragePersistenceTest {

    private static final Gson GSON = new Gson();

    private static final String KEY = "asdf";
    private static final JsonElement VALUE = StoragePersistenceTest.GSON.toJsonTree(new Person("daniel", 21));
    private static final String NEW_KEY = "qwer";
    private static final Object NEW_VALUE = StoragePersistenceTest.GSON.toJsonTree(new Person("sarah", 23));

    private static class Person {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return "Person [name=" + name + ", age=" + age + "]";
        }
    }

    private static UUID STORAGE_ID;

    @Autowired
    private StorageRepository repository;

    @Test
    @Order(1)
    void testSave() {
        Storage storage = new Storage();
        storage.put(StoragePersistenceTest.KEY, StoragePersistenceTest.VALUE);
        assertTrue(storage.containsKey(StoragePersistenceTest.KEY));
        assertEquals(StoragePersistenceTest.VALUE, storage.get(StoragePersistenceTest.KEY));
        StoragePersistenceTest.STORAGE_ID = this.repository.save(storage).getID();
    }

    @Test
    @Order(2)
    void testRetrieve() {
        Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTest.STORAGE_ID);
        assertTrue(storageMaybe.isPresent());
        assertTrue(storageMaybe.get().containsKey(StoragePersistenceTest.KEY));
        assertEquals(StoragePersistenceTest.VALUE, storageMaybe.get().get(StoragePersistenceTest.KEY));
    }

    @Test
    @Order(3)
    void testAdd() {
        Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTest.STORAGE_ID);
        assertTrue(storageMaybe.isPresent());
        storageMaybe.get().put(StoragePersistenceTest.NEW_KEY,
                StoragePersistenceTest.GSON.toJsonTree(StoragePersistenceTest.NEW_VALUE));

        assertTrue(storageMaybe.get().containsKey(StoragePersistenceTest.KEY));
        assertEquals(StoragePersistenceTest.VALUE, storageMaybe.get().get(StoragePersistenceTest.KEY));
        assertTrue(storageMaybe.get().containsKey(StoragePersistenceTest.KEY));
        assertEquals(StoragePersistenceTest.NEW_VALUE,
                storageMaybe.get().get(StoragePersistenceTest.NEW_KEY));
        this.repository.save(storageMaybe.get());
    }

    @Test
    @Order(4)
    void testAddRetrieve() {
        Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTest.STORAGE_ID);
        assertTrue(storageMaybe.isPresent());
        assertTrue(storageMaybe.get().containsKey(StoragePersistenceTest.KEY));
        assertEquals(StoragePersistenceTest.VALUE, storageMaybe.get().get(StoragePersistenceTest.KEY));
        assertTrue(storageMaybe.get().containsKey(StoragePersistenceTest.KEY));
        assertEquals(StoragePersistenceTest.NEW_VALUE,
                storageMaybe.get().get(StoragePersistenceTest.NEW_KEY));
    }

    @Test
    @Order(5)
    void testUpdate() {
        Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTest.STORAGE_ID);
        assertTrue(storageMaybe.isPresent());
        storageMaybe.get().put(StoragePersistenceTest.KEY,
                StoragePersistenceTest.GSON.toJsonTree(StoragePersistenceTest.NEW_VALUE));

        assertTrue(storageMaybe.get().containsKey(StoragePersistenceTest.KEY));
        assertEquals(StoragePersistenceTest.NEW_VALUE,
                storageMaybe.get().get(StoragePersistenceTest.KEY));
        assertTrue(storageMaybe.get().containsKey(StoragePersistenceTest.NEW_KEY));
        assertEquals(StoragePersistenceTest.NEW_VALUE,
                storageMaybe.get().get(StoragePersistenceTest.NEW_KEY));
        this.repository.save(storageMaybe.get());
    }

    @Test
    @Order(6)
    void testUpdateRetrieve() {
        Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTest.STORAGE_ID);
        assertTrue(storageMaybe.isPresent());
        assertTrue(storageMaybe.get().containsKey(StoragePersistenceTest.KEY));
        assertEquals(StoragePersistenceTest.NEW_VALUE,
                storageMaybe.get().get(StoragePersistenceTest.KEY));
        assertTrue(storageMaybe.get().containsKey(StoragePersistenceTest.NEW_KEY));
        assertEquals(StoragePersistenceTest.NEW_VALUE,
                storageMaybe.get().get(StoragePersistenceTest.NEW_KEY));
    }

    @Test
    @Order(7)
    void testDelete() {
        Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTest.STORAGE_ID);
        assertTrue(storageMaybe.isPresent());
        StorageEntry toBeDeleted = storageMaybe.get().remove(StoragePersistenceTest.KEY);
        assertNotNull(toBeDeleted);

        assertFalse(storageMaybe.get().containsKey(StoragePersistenceTest.KEY));
        assertTrue(storageMaybe.get().containsKey(StoragePersistenceTest.NEW_KEY));
        assertEquals(StoragePersistenceTest.NEW_VALUE,
                storageMaybe.get().get(StoragePersistenceTest.NEW_KEY));

        this.repository.save(storageMaybe.get());
    }

    @Test
    @Order(8)
    void testDeleteRetrieve() {
        Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTest.STORAGE_ID);
        assertTrue(storageMaybe.isPresent());
        assertFalse(storageMaybe.get().containsKey(StoragePersistenceTest.KEY));
        assertTrue(storageMaybe.get().containsKey(StoragePersistenceTest.NEW_KEY));
        assertEquals(StoragePersistenceTest.NEW_VALUE,
                storageMaybe.get().get(StoragePersistenceTest.NEW_KEY));
    }
}
