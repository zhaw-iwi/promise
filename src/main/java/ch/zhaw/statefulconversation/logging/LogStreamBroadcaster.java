package ch.zhaw.statefulconversation.logging;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class LogStreamBroadcaster {
    private static LogStreamBroadcaster instance;
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final AtomicLong sendFailureCount = new AtomicLong(0L);

    public LogStreamBroadcaster() {
        LogStreamBroadcaster.instance = this;
    }

    public static LogStreamBroadcaster getInstance() {
        return LogStreamBroadcaster.instance;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        this.emitters.add(emitter);
        emitter.onCompletion(() -> this.unsubscribe(emitter));
        emitter.onTimeout(() -> this.unsubscribe(emitter));
        emitter.onError((ex) -> this.unsubscribe(emitter));
        return emitter;
    }

    public void unsubscribe(SseEmitter emitter) {
        if (emitter == null) {
            return;
        }
        this.emitters.remove(emitter);
    }

    public long getSendFailureCount() {
        return this.sendFailureCount.get();
    }

    public void publish(LogEvent event) {
        if (event == null) {
            return;
        }
        for (SseEmitter emitter : this.emitters) {
            try {
                emitter.send(SseEmitter.event().name("log").data(event));
            } catch (IOException ex) {
                this.handleSendFailure(emitter);
            } catch (Throwable ex) {
                this.handleSendFailure(emitter);
            }
        }
    }

    private void handleSendFailure(SseEmitter emitter) {
        this.sendFailureCount.incrementAndGet();
        this.unsubscribe(emitter);
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
