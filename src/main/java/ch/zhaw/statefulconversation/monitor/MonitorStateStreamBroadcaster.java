package ch.zhaw.statefulconversation.monitor;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import ch.zhaw.statefulconversation.controllers.views.MonitorStateView;

@Component
public class MonitorStateStreamBroadcaster {
    private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emittersByAgent = new ConcurrentHashMap<>();
    private final AtomicLong sendFailureCount = new AtomicLong(0L);

    public SseEmitter subscribe(UUID agentId) {
        SseEmitter emitter = new SseEmitter(0L);
        CopyOnWriteArrayList<SseEmitter> emitters = this.emittersByAgent.computeIfAbsent(agentId,
                (id) -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);
        emitter.onCompletion(() -> this.unsubscribe(agentId, emitter));
        emitter.onTimeout(() -> this.unsubscribe(agentId, emitter));
        emitter.onError((ex) -> this.unsubscribe(agentId, emitter));
        return emitter;
    }

    public void unsubscribe(UUID agentId, SseEmitter emitter) {
        if (agentId == null || emitter == null) {
            return;
        }
        CopyOnWriteArrayList<SseEmitter> emitters = this.emittersByAgent.get(agentId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            this.emittersByAgent.remove(agentId);
        }
    }

    public void publish(UUID agentId, MonitorStateView stateUpdate) {
        if (agentId == null || stateUpdate == null) {
            return;
        }
        CopyOnWriteArrayList<SseEmitter> emitters = this.emittersByAgent.get(agentId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("state").data(stateUpdate));
            } catch (IOException ex) {
                this.handleSendFailure(agentId, emitter);
            } catch (Throwable ex) {
                this.handleSendFailure(agentId, emitter);
            }
        }
    }

    public long getSendFailureCount() {
        return this.sendFailureCount.get();
    }

    private void handleSendFailure(UUID agentId, SseEmitter emitter) {
        this.sendFailureCount.incrementAndGet();
        this.unsubscribe(agentId, emitter);
        if (emitter == null) {
            return;
        }
        try {
            emitter.complete();
        } catch (Throwable ex) {
            // Emitter may already be in terminal state.
        }
    }
}
