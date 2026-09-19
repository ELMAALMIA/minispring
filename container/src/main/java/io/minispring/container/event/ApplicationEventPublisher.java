package io.minispring.container.event;

/**
 * Publishes an event to every interested {@link ApplicationListener}, synchronously, on the
 * caller's thread. Beans receive one through constructor injection.
 */
@FunctionalInterface
public interface ApplicationEventPublisher {

    void publishEvent(ApplicationEvent event);
}
