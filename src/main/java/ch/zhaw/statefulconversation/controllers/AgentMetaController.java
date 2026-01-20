package ch.zhaw.statefulconversation.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.statefulconversation.controllers.dto.SingleStateAgentCreateDTO;
import ch.zhaw.statefulconversation.controllers.views.AgentInfoView;
import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@RestController
public class AgentMetaController {

    @Autowired
    private AgentRepository repository;

    @GetMapping("agent")
    public ResponseEntity<List<AgentInfoView>> findAll() {
        List<Agent> agents = this.repository.findAll();
        List<AgentInfoView> result = new ArrayList<AgentInfoView>();
        for (Agent current : agents) {
            result.add(new AgentInfoView(current.getId(), current.getName(), current.getDescription(),
                    current.isActive()));
        }
        return new ResponseEntity<List<AgentInfoView>>(result, HttpStatus.OK);
    }

    @GetMapping("agent/conversation")
    public ResponseEntity<List<Agent>> findAllConversation() {
        List<Agent> agents = this.repository.findAll();
        return new ResponseEntity<List<Agent>>(agents, HttpStatus.OK);
    }

    @GetMapping("agent/{id}")
    public ResponseEntity<AgentInfoView> findById(@PathVariable UUID id) {
        Optional<Agent> agentMaybe = this.repository.findById(id);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<AgentInfoView>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<AgentInfoView>(
                new AgentInfoView(agentMaybe.get().getId(), agentMaybe.get().getName(),
                        agentMaybe.get().getDescription(), agentMaybe.get().isActive()),
                HttpStatus.OK);
    }

    @GetMapping("agent/{id}/conversation")
    public ResponseEntity<Agent> findByIdConversation(@PathVariable UUID id) {
        Optional<Agent> agentMaybe = this.repository.findById(id);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<Agent>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Agent>(agentMaybe.get(), HttpStatus.OK);
    }

    @PostMapping("agent/singlestate")
    public ResponseEntity<AgentInfoView> create(@RequestBody SingleStateAgentCreateDTO data) {
        Agent agent;
        if (AgentMetaType.singleState.getValue() == data.getType()) {
            agent = AgentMetaUtility.createSingleStateAgent(data);
        } else { // have as many 'else if (...getValue() == data.getType())' as needed here
            System.err.println("unknown agent type " + data.getType());
            return new ResponseEntity<AgentInfoView>(HttpStatus.BAD_REQUEST);
        }

        this.repository.save(agent);

        var result = new AgentInfoView(agent.getId(), agent.getName(), agent.getDescription(), agent.isActive());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
