package ch.zhaw.statefulconversation.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.statefulconversation.controllers.views.AgentInfoView;
import ch.zhaw.statefulconversation.controllers.views.AgentStateInfoView;
import ch.zhaw.statefulconversation.controllers.views.ResponseView;
import ch.zhaw.statefulconversation.controllers.views.StorageEntryView;
import ch.zhaw.statefulconversation.controllers.views.UtteranceRequest;
import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Response;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Utterance;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@RestController
public class AgentController {

    @Autowired
    private AgentRepository repository;

    @GetMapping("{agentID}/info")
    public ResponseEntity<AgentInfoView> info(@PathVariable @NonNull UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<AgentInfoView>(HttpStatus.NOT_FOUND);
        }

        AgentInfoView result = new AgentInfoView(agentMaybe.get().getId(), agentMaybe.get().getName(),
                agentMaybe.get().getDescription(), agentMaybe.get().isActive());

        return new ResponseEntity<AgentInfoView>(result, HttpStatus.OK);
    }

    @GetMapping("{agentID}/conversation")
    public ResponseEntity<List<Utterance>> conversation(@PathVariable @NonNull UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<List<Utterance>>(HttpStatus.NOT_FOUND);
        }

        List<Utterance> conversation = agentMaybe.get().getConversation();

        return new ResponseEntity<List<Utterance>>(conversation, HttpStatus.OK);
    }

    @GetMapping("{agentID}/state")
    public ResponseEntity<AgentStateInfoView> state(@PathVariable @NonNull UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        State currentState = agentMaybe.get().getCurrentState();
        if (currentState == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String stateName = currentState.getName();
        String innerName = null;
        java.util.List<String> innerNames = java.util.List.of();
        if (currentState instanceof ch.zhaw.statefulconversation.model.OuterState outerState
                && outerState.getInnerCurrent() != null) {
            innerName = outerState.getInnerCurrent().getName();
            innerNames = outerState.getInnerCurrentChain();
        }
        AgentStateInfoView stateInfo = new AgentStateInfoView(stateName, innerName, innerNames);

        return new ResponseEntity<>(stateInfo, HttpStatus.OK);
    }

    @GetMapping("{agentID}/states")
    public ResponseEntity<List<String>> states(@PathVariable @NonNull UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(agentMaybe.get().listStates(), HttpStatus.OK);
    }

    @GetMapping("{agentID}/storage")
    public ResponseEntity<List<StorageEntryView>> storage(@PathVariable @NonNull UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<StorageEntryView> entries = agentMaybe.get().getStorage().entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map((entry) -> new StorageEntryView(entry.getKey(),
                        entry.getValue() == null ? "null" : entry.getValue().toString()))
                .toList();

        return new ResponseEntity<>(entries, HttpStatus.OK);
    }

    @PostMapping("{agentID}/start")
    public ResponseEntity<ResponseView> start(@PathVariable @NonNull UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<ResponseView>(HttpStatus.NOT_FOUND);
        }

        Agent agent = agentMaybe.get();
        Response starter = agent.start();
        this.repository.save(agent);

        return new ResponseEntity<ResponseView>(new ResponseView(starter, agent.isActive()),
                HttpStatus.OK);
    }

    @PostMapping("{agentID}/respond")
    public ResponseEntity<ResponseView> respond(@PathVariable @NonNull UUID agentID,
            @RequestBody UtteranceRequest userSays) {

        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<ResponseView>(HttpStatus.NOT_FOUND);
        }
        if (userSays == null || userSays.getContent() == null || userSays.getContent().isBlank()) {
            return new ResponseEntity<ResponseView>(HttpStatus.BAD_REQUEST);
        }

        Agent agent = agentMaybe.orElse(null);
        if (agent == null) {
            return new ResponseEntity<ResponseView>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Response response = agent.respond(userSays.getContent());
        this.repository.save(agent);

        return new ResponseEntity<ResponseView>(new ResponseView(response, agent.isActive()),
                HttpStatus.OK);
    }

    @PostMapping("{agentID}/rerespond")
    public ResponseEntity<ResponseView> ReRespond(@PathVariable @NonNull UUID agentID) {

        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<ResponseView>(HttpStatus.NOT_FOUND);
        }

        Agent agent = agentMaybe.get();
        Response response = agent.reRespond();
        this.repository.save(agent);

        return new ResponseEntity<ResponseView>(new ResponseView(response,
                agent.isActive()), HttpStatus.OK);
    }

    @DeleteMapping("{agentID}/reset")
    public ResponseEntity<ResponseView> reset(@PathVariable @NonNull UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<ResponseView>(HttpStatus.NOT_FOUND);
        }

        Agent agent = agentMaybe.get();
        agent.reset();
        Response response = agent.start();
        this.repository.save(agent);

        return new ResponseEntity<ResponseView>(new ResponseView(response, agent.isActive()),
                HttpStatus.OK);
    }

    @GetMapping("{agentID}/summarise")
    public ResponseEntity<String> summarise(@PathVariable @NonNull UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }

        String summary = agentMaybe.get().summarise();

        return new ResponseEntity<String>(summary, HttpStatus.OK);
    }

}
