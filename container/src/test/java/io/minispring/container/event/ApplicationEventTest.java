package io.minispring.container.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.minispring.container.annotation.PreDestroy;
import io.minispring.container.context.AnnotationApplicationContext;
import io.minispring.container.context.ApplicationContext;
import io.minispring.container.context.ContextClosedEvent;
import io.minispring.container.context.ContextRefreshedEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApplicationEventTest {

    static final List<String> received = new ArrayList<>();

    static class OrderPlaced extends ApplicationEvent {
        final String item;

        OrderPlaced(Object source, String item) {
            super(source);
            this.item = item;
        }
    }

    static class OrderCancelled extends ApplicationEvent {
        OrderCancelled(Object source) {
            super(source);
        }
    }

    static class OrderService {
        private final ApplicationEventPublisher publisher;

        OrderService(ApplicationEventPublisher publisher) {
            this.publisher = publisher;
        }

        void place(String item) {
            publisher.publishEvent(new OrderPlaced(this, item));
        }
    }

    static class ShippingListener implements ApplicationListener<OrderPlaced> {
        @Override
        public void onApplicationEvent(OrderPlaced event) {
            received.add("ship " + event.item);
        }
    }

    static class EverythingListener implements ApplicationListener<ApplicationEvent> {
        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            received.add("saw " + event.getClass().getSimpleName());
        }
    }

    static class LifecycleListener implements ApplicationListener<ApplicationEvent> {
        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof ContextRefreshedEvent || event instanceof ContextClosedEvent) {
                received.add(event.getClass().getSimpleName());
            }
        }
    }

    static class Resource {
        @PreDestroy
        void close() {
            received.add("destroy resource");
        }
    }

    static class FailingListener implements ApplicationListener<OrderPlaced> {
        @Override
        public void onApplicationEvent(OrderPlaced event) {
            throw new IllegalStateException("listener failure");
        }
    }

    static class ContextAware {
        final ApplicationContext context;

        ContextAware(ApplicationContext context) {
            this.context = context;
        }
    }

    @BeforeEach
    void clearReceivedEvents() {
        received.clear();
    }

    @Test
    void aListenerOnlyReceivesTheEventTypeItDeclares() {
        try (var context = AnnotationApplicationContext.of(ShippingListener.class)) {
            context.publishEvent(new OrderCancelled(this));
            context.publishEvent(new OrderPlaced(this, "book"));

            assertThat(received).containsExactly("ship book");
        }
    }

    @Test
    void aListenerForTheBaseTypeReceivesEveryEvent() {
        try (var context = AnnotationApplicationContext.of(EverythingListener.class)) {
            received.clear();

            context.publishEvent(new OrderCancelled(this));

            assertThat(received).containsExactly("saw OrderCancelled");
        }
    }

    @Test
    void beansPublishThroughAnInjectedPublisherWithoutKnowingTheListeners() {
        try (var context = AnnotationApplicationContext.of(OrderService.class, ShippingListener.class)) {
            context.getBean(OrderService.class).place("pen");

            assertThat(received).containsExactly("ship pen");
        }
    }

    @Test
    void announcesTheRefreshAndTheCloseBeforeBeansAreDestroyed() {
        var context = AnnotationApplicationContext.of(LifecycleListener.class, Resource.class);

        context.close();

        assertThat(received).containsExactly("ContextRefreshedEvent", "ContextClosedEvent", "destroy resource");
    }

    @Test
    void aListenerFailurePropagatesToThePublisher() {
        try (var context = AnnotationApplicationContext.of(OrderService.class, FailingListener.class)) {
            OrderService orders = context.getBean(OrderService.class);

            assertThatThrownBy(() -> orders.place("book"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("listener failure");
        }
    }

    @Test
    void theContextItselfCanBeInjected() {
        try (var context = AnnotationApplicationContext.of(ContextAware.class)) {
            assertThat(context.getBean(ContextAware.class).context).isSameAs(context);
        }
    }
}
