package ch.zhaw.statefulconversation.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Utterance;
import ch.zhaw.statefulconversation.repositories.AgentRepository;
import ch.zhaw.statefulconversation.views.AgentInfoView;
import ch.zhaw.statefulconversation.views.ResponseView;

@RestController
public class AgentController {

    @Autowired
    private AgentRepository repository;

    @GetMapping("{agentID}/info")
    public ResponseEntity<AgentInfoView> info(@PathVariable UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<AgentInfoView>(HttpStatus.NOT_FOUND);
        }

        AgentInfoView result = new AgentInfoView(agentMaybe.get().getId(), agentMaybe.get().name(),
                agentMaybe.get().description());

        return new ResponseEntity<AgentInfoView>(result, HttpStatus.OK);
    }

    @GetMapping("{agentID}/conversation")
    public ResponseEntity<List<Utterance>> conversation(@PathVariable UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<List<Utterance>>(HttpStatus.NOT_FOUND);
        }

        List<Utterance> conversation = agentMaybe.get().conversation();

        return new ResponseEntity<List<Utterance>>(conversation, HttpStatus.OK);
    }

    @PostMapping("{agentID}/respond")
    public ResponseEntity<ResponseView> respond(@PathVariable UUID agentID,
            @RequestBody String userSays) {

        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<ResponseView>(HttpStatus.NOT_FOUND);
        }

        String response = agentMaybe.get().respond(userSays);
        this.repository.save(agentMaybe.get());

        return new ResponseEntity<ResponseView>(new ResponseView(List.of(response), agentMaybe.get().isActive()),
                HttpStatus.OK);
    }

    @PostMapping("{agentID}/rerespond")
    public ResponseEntity<ResponseView> ReRespond(@PathVariable UUID agentID) {

        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<ResponseView>(HttpStatus.NOT_FOUND);
        }

        String response = agentMaybe.get().reRespond();
        this.repository.save(agentMaybe.get());

        return new ResponseEntity<ResponseView>(new ResponseView(List.of(response),
                agentMaybe.get().isActive()), HttpStatus.OK);
    }

    @DeleteMapping("{agentID}/reset")
    public ResponseEntity<ResponseView> reset(@PathVariable UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<ResponseView>(HttpStatus.NOT_FOUND);
        }

        agentMaybe.get().reset();
        String response = agentMaybe.get().start();
        this.repository.save(agentMaybe.get());

        return new ResponseEntity<ResponseView>(new ResponseView(List.of(response), agentMaybe.get().isActive()),
                HttpStatus.OK);
    }

    @DeleteMapping("{agentID}/summarise")
    public ResponseEntity<ResponseView> summarise(@PathVariable UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<ResponseView>(HttpStatus.NOT_FOUND);
        }

        String summary = agentMaybe.get().summarise();

        return new ResponseEntity<ResponseView>(new ResponseView(List.of(summary), true), HttpStatus.OK);
    }

    @GetMapping("all")
    public ResponseEntity<List<AgentInfoView>> findAll() {
        List<Agent> agents = this.repository.findAll();
        List<AgentInfoView> result = new ArrayList<AgentInfoView>();
        for (Agent current : agents) {
            result.add(new AgentInfoView(current.getId(), current.name(), current.description()));
        }
        return new ResponseEntity<List<AgentInfoView>>(result, HttpStatus.OK);
    }
}
