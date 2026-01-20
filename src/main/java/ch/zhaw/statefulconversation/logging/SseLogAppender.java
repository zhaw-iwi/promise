package ch.zhaw.statefulconversation.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class SseLogAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent eventObject) {
        LogStreamBroadcaster broadcaster = LogStreamBroadcaster.getInstance();
        if (broadcaster == null) {
            return;
        }
        LogEvent event = new LogEvent(
                eventObject.getTimeStamp(),
                eventObject.getLevel().toString(),
                eventObject.getLoggerName(),
                eventObject.getFormattedMessage());
        broadcaster.publish(event);
    }
}
