package ch.zhaw.statefulconversation.monitor;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import ch.zhaw.statefulconversation.controllers.views.MonitorStateView;
import ch.zhaw.statefulconversation.model.Agent;
import ch.zhaw.statefulconversation.repositories.AgentRepository;

@RestController
public class MonitorStateStreamController {

    @Autowired
    private AgentRepository repository;

    @Autowired
    private MonitorStateStreamBroadcaster broadcaster;

    @GetMapping(path = "{agentID}/state/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> stream(@PathVariable UUID agentID) {
        if (agentID == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<Agent> agentMaybe = this.repository.findById(agentID);
        if (agentMaybe.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        SseEmitter emitter = this.broadcaster.subscribe(agentID);
        MonitorStateView stateUpdate = MonitorStateViewBuilder.fromAgent(agentMaybe.get());
        try {
            emitter.send(SseEmitter.event().name("state").data(stateUpdate));
        } catch (Throwable ex) {
            this.broadcaster.unsubscribe(agentID, emitter);
            try {
                emitter.complete();
            } catch (Throwable completeEx) {
                // Emitter may already be in terminal state.
            }
        }
        return new ResponseEntity<>(emitter, HttpStatus.OK);
    }
}
