package ch.zhaw.statefulconversation.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
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
import ch.zhaw.statefulconversation.repositories.StorageRepository;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
public class StoragePersistenceTestList {

        private static final Gson GSON = new Gson();

        private static final String KEY = "asdf";
        private static final JsonElement VALUE = StoragePersistenceTestList.GSON
                        .toJsonTree(List.of("Müdigkeit bei der Arbeit.", "Nebenwirkungen der Coronaimpfung"));
        private static final Object NEW_VALUE = StoragePersistenceTestList.GSON
                        .toJsonTree(List.of("Nebenwirkungen der Coronaimpfung"));

        private static UUID STORAGE_ID;

        @Autowired
        private StorageRepository repository;

        @Test
        @Order(1)
        void testSave() {
                Storage storage = new Storage();
                storage.put(StoragePersistenceTestList.KEY, StoragePersistenceTestList.VALUE);
                assertTrue(storage.containsKey(StoragePersistenceTestList.KEY));
                assertEquals(StoragePersistenceTestList.VALUE, storage.get(StoragePersistenceTestList.KEY));
                StoragePersistenceTestList.STORAGE_ID = this.repository.save(storage).getID();
        }

        @Test
        @Order(2)
        void testRetrieve() {
                Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTestList.STORAGE_ID);
                assertTrue(storageMaybe.isPresent());
                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                assertEquals(StoragePersistenceTestList.VALUE, storageMaybe.get().get(StoragePersistenceTestList.KEY));
        }

        @Test
        @Order(3)
        void testAdd() {
                Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTestList.STORAGE_ID);
                assertTrue(storageMaybe.isPresent());
                storageMaybe.get().put(StoragePersistenceTestList.KEY,
                                StoragePersistenceTestList.GSON.toJsonTree(StoragePersistenceTestList.NEW_VALUE));

                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                assertEquals(StoragePersistenceTestList.NEW_VALUE,
                                storageMaybe.get().get(StoragePersistenceTestList.KEY));
                this.repository.save(storageMaybe.get());
        }

        @Test
        @Order(4)
        void testAddRetrieve() {
                Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTestList.STORAGE_ID);
                assertTrue(storageMaybe.isPresent());
                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                assertEquals(StoragePersistenceTestList.NEW_VALUE,
                                storageMaybe.get().get(StoragePersistenceTestList.KEY));
                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                assertEquals(StoragePersistenceTestList.NEW_VALUE,
                                storageMaybe.get().get(StoragePersistenceTestList.KEY));
        }

        @Test
        @Order(5)
        void testUpdate() {
                Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTestList.STORAGE_ID);
                assertTrue(storageMaybe.isPresent());
                storageMaybe.get().put(StoragePersistenceTestList.KEY,
                                StoragePersistenceTestList.GSON.toJsonTree(StoragePersistenceTestList.NEW_VALUE));

                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                assertEquals(StoragePersistenceTestList.NEW_VALUE,
                                storageMaybe.get().get(StoragePersistenceTestList.KEY));
                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                assertEquals(StoragePersistenceTestList.NEW_VALUE,
                                storageMaybe.get().get(StoragePersistenceTestList.KEY));
                this.repository.save(storageMaybe.get());
        }

        @Test
        @Order(6)
        void testUpdateRetrieve() {
                Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTestList.STORAGE_ID);
                assertTrue(storageMaybe.isPresent());
                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                assertEquals(StoragePersistenceTestList.NEW_VALUE,
                                storageMaybe.get().get(StoragePersistenceTestList.KEY));
                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                assertEquals(StoragePersistenceTestList.NEW_VALUE,
                                storageMaybe.get().get(StoragePersistenceTestList.KEY));
        }

        @Test
        @Order(7)
        void testDeleteRetrieve() {
                Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTestList.STORAGE_ID);
                assertTrue(storageMaybe.isPresent());
                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                assertEquals(StoragePersistenceTestList.NEW_VALUE,
                                storageMaybe.get().get(StoragePersistenceTestList.KEY));
        }
}
