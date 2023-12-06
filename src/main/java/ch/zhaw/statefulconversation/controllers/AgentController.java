package ch.zhaw.statefulconversation.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.model.Utterance;
import ch.zhaw.statefulconversation.repositories.AgentRepository;
import ch.zhaw.statefulconversation.views.AgentInfoView;
import ch.zhaw.statefulconversation.views.ResponseView;

@RestController
public class AgentController {

    @Autowired
    private AgentRepository repository;

    @RequestMapping(value = "{agentID}/info", method = RequestMethod.GET)
    public ResponseEntity<AgentInfoView> info(@PathVariable("agentID") UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (!agentMaybe.isPresent()) {
            return new ResponseEntity<AgentInfoView>(HttpStatus.NOT_FOUND);
        }

        AgentInfoView result = new AgentInfoView(agentMaybe.get().getId(), agentMaybe.get().name(),
                agentMaybe.get().description());

        return new ResponseEntity<AgentInfoView>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "{agentID}/conversation", method = RequestMethod.GET)
    public ResponseEntity<List<Utterance>> conversation(@PathVariable("agentID") UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (!agentMaybe.isPresent()) {
            return new ResponseEntity<List<Utterance>>(HttpStatus.NOT_FOUND);
        }

        List<Utterance> conversation = agentMaybe.get().conversation();

        return new ResponseEntity<List<Utterance>>(conversation, HttpStatus.OK);
    }

    @RequestMapping(value = "{agentID}/respond", method = RequestMethod.POST)
    public ResponseEntity<ResponseView> respond(@PathVariable("agentID") UUID agentID,
            @RequestBody String userSays) {

        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (!agentMaybe.isPresent()) {
            return new ResponseEntity<ResponseView>(HttpStatus.NOT_FOUND);
        }

        String response = agentMaybe.get().respond(userSays);
        this.repository.save(agentMaybe.get());

        return new ResponseEntity<ResponseView>(new ResponseView(List.of(response), agentMaybe.get().isActive()),
                HttpStatus.OK);
    }

    @RequestMapping(value = "{agentID}/rerespond", method = RequestMethod.POST)
    public ResponseEntity<ResponseView> ReRespond(@PathVariable("agentID") UUID agentID) {

        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (!agentMaybe.isPresent()) {
            return new ResponseEntity<ResponseView>(HttpStatus.NOT_FOUND);
        }

        String response = agentMaybe.get().reRespond();
        this.repository.save(agentMaybe.get());

        return new ResponseEntity<ResponseView>(new ResponseView(List.of(response),
                agentMaybe.get().isActive()), HttpStatus.OK);
    }

    @RequestMapping(value = "{agentID}/reset", method = RequestMethod.DELETE)
    public ResponseEntity<ResponseView> reset(@PathVariable("agentID") UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (!agentMaybe.isPresent()) {
            return new ResponseEntity<ResponseView>(HttpStatus.NOT_FOUND);
        }

        agentMaybe.get().reset();
        String response = agentMaybe.get().start();
        this.repository.save(agentMaybe.get());

        return new ResponseEntity<ResponseView>(new ResponseView(List.of(response), agentMaybe.get().isActive()),
                HttpStatus.OK);
    }

    @RequestMapping(value = "{agentID}/summarise", method = RequestMethod.DELETE)
    public ResponseEntity<ResponseView> summarise(@PathVariable("agentID") UUID agentID) {
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (!agentMaybe.isPresent()) {
            return new ResponseEntity<ResponseView>(HttpStatus.NOT_FOUND);
        }

        String summary = agentMaybe.get().summarise();

        return new ResponseEntity<ResponseView>(new ResponseView(List.of(summary), true), HttpStatus.OK);
    }

    @RequestMapping(value = "all", method = RequestMethod.GET)
    public ResponseEntity<List<AgentInfoView>> findAll() {
        List<Agent> agents = this.repository.findAll();
        List<AgentInfoView> result = new ArrayList<AgentInfoView>();
        for (Agent current : agents) {
            result.add(new AgentInfoView(current.getId(), current.name(), current.description()));
        }
        return new ResponseEntity<List<AgentInfoView>>(result, HttpStatus.OK);
    }
}
