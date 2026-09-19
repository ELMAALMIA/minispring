package io.minispring.container.context;

import static org.assertj.core.api.Assertions.assertThat;

import io.minispring.container.annotation.Scope;
import io.minispring.container.annotation.ScopeType;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScopeTest {

    static final AtomicInteger SINGLETONS_CREATED = new AtomicInteger();
    static final AtomicInteger PROTOTYPES_CREATED = new AtomicInteger();

    static class Cache {
        Cache() {
            SINGLETONS_CREATED.incrementAndGet();
        }
    }

    @Scope(ScopeType.PROTOTYPE)
    static class ShoppingCart {
        ShoppingCart() {
            PROTOTYPES_CREATED.incrementAndGet();
        }
    }

    static class Checkout {
        final ShoppingCart cart;

        Checkout(ShoppingCart cart) {
            this.cart = cart;
        }
    }

    @BeforeEach
    void resetCounters() {
        SINGLETONS_CREATED.set(0);
        PROTOTYPES_CREATED.set(0);
    }

    @Test
    void returnsTheSameSingletonOnEveryLookup() {
        try (var context = AnnotationApplicationContext.of(Cache.class)) {
            assertThat(context.getBean(Cache.class)).isSameAs(context.getBean(Cache.class));
        }
    }

    @Test
    void returnsANewPrototypeOnEveryLookup() {
        try (var context = AnnotationApplicationContext.of(ShoppingCart.class)) {
            assertThat(context.getBean(ShoppingCart.class)).isNotSameAs(context.getBean(ShoppingCart.class));
        }
    }

    @Test
    void createsSingletonsAtStartupAndPrototypesOnlyWhenRequested() {
        try (var context = AnnotationApplicationContext.of(Cache.class, ShoppingCart.class)) {
            assertThat(SINGLETONS_CREATED).hasValue(1);
            assertThat(PROTOTYPES_CREATED).hasValue(0);

            context.getBean(ShoppingCart.class);

            assertThat(PROTOTYPES_CREATED).hasValue(1);
        }
    }

    @Test
    void injectsAPrototypeIntoASingletonOnlyOnce() {
        try (var context = AnnotationApplicationContext.of(Checkout.class, ShoppingCart.class)) {
            Checkout checkout = context.getBean(Checkout.class);

            assertThat(context.getBean(Checkout.class).cart).isSameAs(checkout.cart);
            assertThat(context.getBean(ShoppingCart.class)).isNotSameAs(checkout.cart);
        }
    }
}
