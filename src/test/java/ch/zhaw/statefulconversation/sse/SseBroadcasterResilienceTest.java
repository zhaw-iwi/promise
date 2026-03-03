package ch.zhaw.statefulconversation.sse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import ch.zhaw.statefulconversation.controllers.views.AgentStateInfoView;
import ch.zhaw.statefulconversation.controllers.views.MonitorStateView;
import ch.zhaw.statefulconversation.controllers.views.StorageEntryView;
import ch.zhaw.statefulconversation.logging.LogEvent;
import ch.zhaw.statefulconversation.logging.LogStreamBroadcaster;
import ch.zhaw.statefulconversation.monitor.MonitorStateStreamBroadcaster;
import ch.zhaw.statefulconversation.storage.StorageStreamBroadcaster;

public class SseBroadcasterResilienceTest {

    @Test
    void logBroadcaster_publishDoesNotThrowAndRemovesFailedEmitter() throws Exception {
        LogStreamBroadcaster broadcaster = new LogStreamBroadcaster();
        ThrowingEmitter emitter = new ThrowingEmitter();
        CopyOnWriteArrayList<SseEmitter> emitters = getLogEmitters(broadcaster);
        emitters.add(emitter);

        assertDoesNotThrow(() -> broadcaster.publish(new LogEvent(System.currentTimeMillis(), "INFO", "test.logger", "x")));
        assertTrue(emitter.wasCompleted());
        assertFalse(emitters.contains(emitter));
        assertEquals(1L, broadcaster.getSendFailureCount());
    }

    @Test
    void monitorBroadcaster_publishDoesNotThrowAndRemovesFailedEmitter() throws Exception {
        MonitorStateStreamBroadcaster broadcaster = new MonitorStateStreamBroadcaster();
        UUID agentId = UUID.randomUUID();
        ThrowingEmitter emitter = new ThrowingEmitter();
        Map<UUID, CopyOnWriteArrayList<SseEmitter>> emittersByAgent = getEmittersByAgent(broadcaster);
        emittersByAgent.put(agentId, new CopyOnWriteArrayList<>(List.of(emitter)));

        MonitorStateView stateUpdate = new MonitorStateView(new AgentStateInfoView("A", null, List.of()), true);
        assertDoesNotThrow(() -> broadcaster.publish(agentId, stateUpdate));
        assertTrue(emitter.wasCompleted());
        assertFalse(emittersByAgent.containsKey(agentId));
        assertEquals(1L, broadcaster.getSendFailureCount());
    }

    @Test
    void storageBroadcaster_publishDoesNotThrowAndRemovesFailedEmitter() throws Exception {
        StorageStreamBroadcaster broadcaster = new StorageStreamBroadcaster();
        UUID agentId = UUID.randomUUID();
        ThrowingEmitter emitter = new ThrowingEmitter();
        Map<UUID, CopyOnWriteArrayList<SseEmitter>> emittersByAgent = getEmittersByAgent(broadcaster);
        emittersByAgent.put(agentId, new CopyOnWriteArrayList<>(List.of(emitter)));

        List<StorageEntryView> entries = List.of(new StorageEntryView("k", "v"));
        assertDoesNotThrow(() -> broadcaster.publish(agentId, entries));
        assertTrue(emitter.wasCompleted());
        assertFalse(emittersByAgent.containsKey(agentId));
        assertEquals(1L, broadcaster.getSendFailureCount());
    }

    @SuppressWarnings("unchecked")
    private static CopyOnWriteArrayList<SseEmitter> getLogEmitters(LogStreamBroadcaster broadcaster) throws Exception {
        Field field = LogStreamBroadcaster.class.getDeclaredField("emitters");
        field.setAccessible(true);
        return (CopyOnWriteArrayList<SseEmitter>) field.get(broadcaster);
    }

    @SuppressWarnings("unchecked")
    private static Map<UUID, CopyOnWriteArrayList<SseEmitter>> getEmittersByAgent(Object broadcaster) throws Exception {
        Field field = broadcaster.getClass().getDeclaredField("emittersByAgent");
        field.setAccessible(true);
        return (Map<UUID, CopyOnWriteArrayList<SseEmitter>>) field.get(broadcaster);
    }

    private static final class ThrowingEmitter extends SseEmitter {
        private volatile boolean completed;

        private ThrowingEmitter() {
            super(0L);
            this.completed = false;
        }

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            throw new IOException("simulated disconnect");
        }

        @Override
        public synchronized void complete() {
            this.completed = true;
        }

        private boolean wasCompleted() {
            return this.completed;
        }
    }
}
