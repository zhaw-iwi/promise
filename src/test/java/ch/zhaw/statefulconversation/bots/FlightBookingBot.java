package ch.zhaw.statefulconversation.bots;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.Gson;

import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Decision;
import ch.zhaw.statefulconversation.model.Final;
import ch.zhaw.statefulconversation.model.OuterState;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Storage;
import ch.zhaw.statefulconversation.model.Transition;
import ch.zhaw.statefulconversation.model.commons.decisions.StaticDecision;
import ch.zhaw.statefulconversation.model.commons.states.DynamicGatherState;
import ch.zhaw.statefulconversation.model.commons.states.DynamicSingleChoiceState;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@SpringBootTest
public class FlightBookingBot {

        @Autowired
        private AgentRepository repository;

        @Test
        void setUp() {

                String storageKeyFromSlots = "OrderSlots";
                String storageKeyFromChoices = "Offers";
                String storageKeyToChoice = "OffersChosen";
                String storageKeyToSlotValues = "OrderSlotsValues";

                Gson gson = new Gson();

                Storage storage = new Storage();
                storage.put(storageKeyFromSlots, gson.toJsonTree(List.of("Departure", "Destination", "Date")));
                storage.put(storageKeyFromChoices,
                                gson.toJsonTree(List.of("Bamboo Airways", "Pegasus Airlines", "French Bee")));

                State usersChooseOffer = new DynamicSingleChoiceState("UsersChooseOffer", new Final(), storage,
                                storageKeyFromChoices, storageKeyToChoice);
                State usersSayFromToDate = new DynamicGatherState("UsersSayFromToDate", usersChooseOffer, storage,
                                storageKeyFromSlots, storageKeyToSlotValues);

                Decision outerStateTrigger = new StaticDecision(
                                "Review the chat and determine if the user wants to exit.");
                Transition outerStateTransition = new Transition(List.of(outerStateTrigger), List.of(), new Final());
                State outerState = new OuterState("You are a grumpy flight booking assistant.", "OuterState",
                                List.of(outerStateTransition),
                                usersSayFromToDate);

                Agent agent = new Agent("Grumpy Flight Booking Assistant",
                                "Grumpy flight booking assistant helping you book a flight.", outerState,
                                storage);
                agent.start();
                this.repository.save(agent);
        }
}
