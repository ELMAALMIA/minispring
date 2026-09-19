package io.minispring.container.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.minispring.container.annotation.Transactional;
import io.minispring.container.context.AnnotationApplicationContext;
import io.minispring.container.exception.BeanCreationException;
import io.minispring.container.exception.BeanNotOfRequiredTypeException;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TransactionalTest {

    static class RecordingTransactionManager implements TransactionManager {
        final List<String> events = new ArrayList<>();

        @Override
        public void begin(String name) {
            events.add("begin " + name);
        }

        @Override
        public void commit(String name) {
            events.add("commit " + name);
        }

        @Override
        public void rollback(String name) {
            events.add("rollback " + name);
        }
    }

    interface OrderService {
        String placeOrder(String item);

        void placeTwoOrders(String first, String second);

        String describe();

        void importOrders() throws IOException;
    }

    static class OrderServiceImpl implements OrderService {
        @Override
        @Transactional
        public String placeOrder(String item) {
            if (item.isBlank()) {
                throw new IllegalArgumentException("item must not be blank");
            }
            return "ordered " + item;
        }

        @Override
        public void placeTwoOrders(String first, String second) {
            // Calls on "this" never reach the proxy.
            placeOrder(first);
            placeOrder(second);
        }

        @Override
        public String describe() {
            return "order service";
        }

        @Override
        @Transactional
        public void importOrders() throws IOException {
            throw new IOException("orders.csv is missing");
        }
    }

    static class Checkout {
        final OrderService orders;

        Checkout(OrderService orders) {
            this.orders = orders;
        }
    }

    static class NeedsImplementation {
        NeedsImplementation(OrderServiceImpl orders) {
        }
    }

    interface Greeter {
        String greet();
    }

    static class PlainGreeter implements Greeter {
        @Override
        public String greet() {
            return "hello";
        }
    }

    static class NoInterfaceService {
        @Transactional
        public void run() {
        }
    }

    static class HiddenTransactionalMethod implements Greeter {
        @Override
        public String greet() {
            return "hello";
        }

        @Transactional
        public void notOnTheInterface() {
        }
    }

    @Test
    void aBeanWithoutTransactionalMethodsIsNotProxied() {
        try (var context = AnnotationApplicationContext.of(PlainGreeter.class)) {
            assertThat(context.getBean(Greeter.class)).isInstanceOf(PlainGreeter.class);
        }
    }

    @Test
    void aBeanWithATransactionalMethodIsProxiedBehindItsInterface() {
        try (var context = AnnotationApplicationContext.of(OrderServiceImpl.class, RecordingTransactionManager.class)) {
            OrderService orders = context.getBean(OrderService.class);

            assertThat(Proxy.isProxyClass(orders.getClass())).isTrue();
            assertThat(orders).isNotInstanceOf(OrderServiceImpl.class);
        }
    }

    @Test
    void aSuccessfulCallBeginsThenCommits() {
        try (var context = AnnotationApplicationContext.of(OrderServiceImpl.class, RecordingTransactionManager.class)) {
            String result = context.getBean(OrderService.class).placeOrder("book");

            assertThat(result).isEqualTo("ordered book");
            assertThat(context.getBean(RecordingTransactionManager.class).events)
                    .containsExactly("begin OrderServiceImpl.placeOrder", "commit OrderServiceImpl.placeOrder");
        }
    }

    @Test
    void aRuntimeExceptionRollsBackAndPropagatesTheOriginalException() {
        try (var context = AnnotationApplicationContext.of(OrderServiceImpl.class, RecordingTransactionManager.class)) {
            OrderService orders = context.getBean(OrderService.class);

            assertThatThrownBy(() -> orders.placeOrder(" "))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("item must not be blank");
            assertThat(context.getBean(RecordingTransactionManager.class).events)
                    .containsExactly("begin OrderServiceImpl.placeOrder", "rollback OrderServiceImpl.placeOrder");
        }
    }

    @Test
    void aCheckedExceptionCommitsLikeSpringDoes() {
        try (var context = AnnotationApplicationContext.of(OrderServiceImpl.class, RecordingTransactionManager.class)) {
            OrderService orders = context.getBean(OrderService.class);

            assertThatThrownBy(orders::importOrders).isExactlyInstanceOf(IOException.class);
            assertThat(context.getBean(RecordingTransactionManager.class).events)
                    .containsExactly("begin OrderServiceImpl.importOrders", "commit OrderServiceImpl.importOrders");
        }
    }

    @Test
    void aMethodWithoutTheAnnotationRunsWithoutATransaction() {
        try (var context = AnnotationApplicationContext.of(OrderServiceImpl.class, RecordingTransactionManager.class)) {
            assertThat(context.getBean(OrderService.class).describe()).isEqualTo("order service");
            assertThat(context.getBean(RecordingTransactionManager.class).events).isEmpty();
        }
    }

    @Test
    void selfInvocationBypassesTheProxyAndStartsNoTransaction() {
        try (var context = AnnotationApplicationContext.of(OrderServiceImpl.class, RecordingTransactionManager.class)) {
            context.getBean(OrderService.class).placeTwoOrders("book", "pen");

            assertThat(context.getBean(RecordingTransactionManager.class).events).isEmpty();
        }
    }

    @Test
    void dependentsReceiveTheProxy() {
        try (var context = AnnotationApplicationContext.of(Checkout.class, OrderServiceImpl.class, RecordingTransactionManager.class)) {
            context.getBean(Checkout.class).orders.placeOrder("book");

            assertThat(context.getBean(RecordingTransactionManager.class).events).hasSize(2);
        }
    }

    @Test
    void dependingOnTheImplementationClassExplainsTheProxy() {
        assertThatThrownBy(() -> AnnotationApplicationContext.of(NeedsImplementation.class, OrderServiceImpl.class))
                .isInstanceOf(BeanNotOfRequiredTypeException.class)
                .hasMessageContaining("Bean 'orderServiceImpl' is a")
                .hasMessageContaining("JDK dynamic proxy")
                .hasMessageContaining("[OrderService]");
    }

    @Test
    void aTransactionalBeanWithoutAnInterfaceFailsAtStartup() {
        assertThatThrownBy(() -> AnnotationApplicationContext.of(NoInterfaceService.class))
                .isInstanceOf(BeanCreationException.class)
                .hasMessageContaining("Cannot create bean 'noInterfaceService'")
                .hasMessageContaining("implements no interface");
    }

    @Test
    void aTransactionalMethodMissingFromTheInterfacesFailsAtStartup() {
        assertThatThrownBy(() -> AnnotationApplicationContext.of(HiddenTransactionalMethod.class))
                .isInstanceOf(BeanCreationException.class)
                .hasMessageContaining("@Transactional method notOnTheInterface() is not declared by any of its interfaces [Greeter]");
    }
}
