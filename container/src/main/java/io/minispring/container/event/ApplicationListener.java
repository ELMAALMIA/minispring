package io.minispring.container.event;

/**
 * A bean that observes events of type {@code E}. The container reads {@code E} from the class
 * declaration, so the listener only receives events it can handle.
 *
 * @param <E> the event type this listener handles, subclasses included
 */
@FunctionalInterface
public interface ApplicationListener<E extends ApplicationEvent> {

    void onApplicationEvent(E event);
}
