package ch.zhaw.statefulconversation.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
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
class StoragePersistenceTestList {

        private static final Gson GSON = new Gson();

        private static final String KEY = "asdf";
        private static final List<String> LIST_OF_TWO = List.of("Müdigkeit bei der Arbeit.",
                        "Nebenwirkungen der Coronaimpfung");
        private static final List<String> LIST_OF_ONE = List.of("Nebenwirkungen der Coronaimpfung");
        private static final String ITEM_REMOVED = "Müdigkeit bei der Arbeit.";

        private static UUID STORAGE_ID;

        @Autowired
        private StorageRepository repository;

        private static List<String> jsonElementToList(JsonElement jsonListOfStrings) {
                List<JsonElement> listOfJsonElements = jsonListOfStrings.getAsJsonArray().asList();
                List<String> result = new ArrayList<String>();
                for (JsonElement current : listOfJsonElements) {
                        result.add(current.getAsString());
                }
                return result;
        }

        @Test
        @Order(1)
        void save() {
                Storage storage = new Storage();
                storage.put(StoragePersistenceTestList.KEY,
                                StoragePersistenceTestList.GSON.toJsonTree(StoragePersistenceTestList.LIST_OF_TWO));
                assertTrue(storage.containsKey(StoragePersistenceTestList.KEY));
                assertEquals(StoragePersistenceTestList.LIST_OF_TWO,
                                StoragePersistenceTestList
                                                .jsonElementToList(storage.get(StoragePersistenceTestList.KEY)));
                StoragePersistenceTestList.STORAGE_ID = this.repository.save(storage).getID();
        }

        @Test
        @Order(2)
        void retrieve() {
                Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTestList.STORAGE_ID);
                assertTrue(storageMaybe.isPresent());
                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                assertEquals(StoragePersistenceTestList.LIST_OF_TWO,
                                StoragePersistenceTestList
                                                .jsonElementToList(storageMaybe.get()
                                                                .get(StoragePersistenceTestList.KEY)));
        }

        @Test
        @Order(3)
        void removeFromList() {
                Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTestList.STORAGE_ID);
                assertTrue(storageMaybe.isPresent());
                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                JsonElement listAsJsonElement = storageMaybe.get().get(StoragePersistenceTestList.KEY);
                List<String> list = StoragePersistenceTestList.jsonElementToList(listAsJsonElement);

                list.remove(StoragePersistenceTestList.ITEM_REMOVED);
                // useless test whether that was removed in java list in memory
                assertEquals(StoragePersistenceTestList.LIST_OF_ONE, list);

                storageMaybe.get().put(StoragePersistenceTestList.KEY,
                                StoragePersistenceTestList.GSON.toJsonTree(list));
                this.repository.save(storageMaybe.get());

                // test whether removal still effective if retrieved from storage again
                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                assertEquals(StoragePersistenceTestList.LIST_OF_ONE, StoragePersistenceTestList.jsonElementToList(
                                storageMaybe.get().get(StoragePersistenceTestList.KEY)));
        }

        @Test
        @Order(4)
        void removeFromListRetrieve() {
                Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTestList.STORAGE_ID);
                assertTrue(storageMaybe.isPresent());
                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                JsonElement listAsJsonElement = storageMaybe.get().get(StoragePersistenceTestList.KEY);
                List<String> list = StoragePersistenceTestList.jsonElementToList(listAsJsonElement);

                // usefull test whether that was removed in storage retrieved from db
                assertEquals(StoragePersistenceTestList.LIST_OF_ONE, list);
        }

        @Test
        @Order(5)
        void addToList() {
                Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTestList.STORAGE_ID);
                assertTrue(storageMaybe.isPresent());
                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                JsonElement listAsJsonElement = storageMaybe.get().get(StoragePersistenceTestList.KEY);
                List<String> list = StoragePersistenceTestList.jsonElementToList(listAsJsonElement);

                list.addFirst(StoragePersistenceTestList.ITEM_REMOVED);
                // useless test whether that was added in java list in memory
                assertEquals(StoragePersistenceTestList.LIST_OF_TWO, list);

                storageMaybe.get().put(StoragePersistenceTestList.KEY,
                                StoragePersistenceTestList.GSON.toJsonTree(list));
                this.repository.save(storageMaybe.get());

                // test whether addition still effective if retrieved from storage again
                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                assertEquals(StoragePersistenceTestList.LIST_OF_TWO, StoragePersistenceTestList.jsonElementToList(
                                storageMaybe.get().get(StoragePersistenceTestList.KEY)));
        }

        @Test
        @Order(6)
        void addToListRetrieve() {
                Optional<Storage> storageMaybe = this.repository.findById(StoragePersistenceTestList.STORAGE_ID);
                assertTrue(storageMaybe.isPresent());
                assertTrue(storageMaybe.get().containsKey(StoragePersistenceTestList.KEY));
                JsonElement listAsJsonElement = storageMaybe.get().get(StoragePersistenceTestList.KEY);
                List<String> list = StoragePersistenceTestList.jsonElementToList(listAsJsonElement);

                // usefull test whether that was added in storage retrieved from db
                assertEquals(StoragePersistenceTestList.LIST_OF_TWO, list);
        }

}
