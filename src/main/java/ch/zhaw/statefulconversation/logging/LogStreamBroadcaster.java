package ch.zhaw.statefulconversation.logging;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class LogStreamBroadcaster {
    private static LogStreamBroadcaster instance;
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public LogStreamBroadcaster() {
        LogStreamBroadcaster.instance = this;
    }

    public static LogStreamBroadcaster getInstance() {
        return LogStreamBroadcaster.instance;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        this.emitters.add(emitter);
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError((ex) -> this.emitters.remove(emitter));
        return emitter;
    }

    public void publish(LogEvent event) {
        for (SseEmitter emitter : this.emitters) {
            try {
                emitter.send(SseEmitter.event().name("log").data(event));
            } catch (IOException ex) {
                emitter.complete();
                this.emitters.remove(emitter);
            } catch (Exception ex) {
                emitter.completeWithError(ex);
                this.emitters.remove(emitter);
            }
        }
    }
}
