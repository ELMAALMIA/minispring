package io.minispring.container.event;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * The subject of the Observer pattern: it delivers each event to the listeners that declared
 * a matching event type.
 *
 * <p>Listeners are looked up at publication time rather than registered up front, so an event
 * published while the context is still starting reaches every listener, including listeners
 * that do not exist yet.
 */
public final class ApplicationEventMulticaster implements ApplicationEventPublisher {

    /** The event type each listener class declares, computed once per class. */
    private static final ClassValue<Class<?>> DECLARED_EVENT_TYPES = new ClassValue<>() {
        @Override
        protected Class<?> computeValue(Class<?> listenerClass) {
            return declaredEventType(listenerClass);
        }
    };

    private final Supplier<? extends Collection<? extends ApplicationListener<?>>> listeners;

    public ApplicationEventMulticaster(Supplier<? extends Collection<? extends ApplicationListener<?>>> listeners) {
        this.listeners = Objects.requireNonNull(listeners, "listeners");
    }

    @Override
    public void publishEvent(ApplicationEvent event) {
        Objects.requireNonNull(event, "event");
        for (ApplicationListener<?> listener : listeners.get()) {
            if (DECLARED_EVENT_TYPES.get(listener.getClass()).isInstance(event)) {
                deliver(listener, event);
            }
        }
    }

    @SuppressWarnings("unchecked") // Safe: the listener's declared event type was checked by the caller.
    private static void deliver(ApplicationListener<?> listener, ApplicationEvent event) {
        ((ApplicationListener<ApplicationEvent>) listener).onApplicationEvent(event);
    }

    /**
     * Reads {@code E} from {@code implements ApplicationListener<E>} in the class or one of its
     * superclasses. A listener whose type argument cannot be read receives every event.
     */
    private static Class<?> declaredEventType(Class<?> listenerClass) {
        for (Class<?> current = listenerClass; current != null; current = current.getSuperclass()) {
            for (Type type : current.getGenericInterfaces()) {
                if (type instanceof ParameterizedType parameterized
                        && parameterized.getRawType() == ApplicationListener.class
                        && parameterized.getActualTypeArguments()[0] instanceof Class<?> eventType) {
                    return eventType;
                }
            }
        }
        return ApplicationEvent.class;
    }
}
