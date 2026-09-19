package io.minispring.container.event;

import java.time.Instant;
import java.util.Objects;

/**
 * Base class of the events published through the context.
 *
 * <p>Events implement the Observer pattern: a publisher does not know who listens, and a
 * listener does not know who publishes. Each side depends only on the event type.
 */
public abstract class ApplicationEvent {

    private final Object source;
    private final Instant timestamp;

    protected ApplicationEvent(Object source) {
        this.source = Objects.requireNonNull(source, "source");
        this.timestamp = Instant.now();
    }

    /** The object that published the event. */
    public Object getSource() {
        return source;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
