package io.minispring.container.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.minispring.container.annotation.Bean;
import io.minispring.container.annotation.Configuration;
import io.minispring.container.context.AnnotationApplicationContext;
import io.minispring.container.env.StandardEnvironment;
import io.minispring.container.exception.BeanDefinitionException;
import io.minispring.container.exception.NoSuchBeanException;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConditionalTest {

    static class Always implements Condition {
        @Override
        public ConditionOutcome matches(ConditionContext context, AnnotatedElement element) {
            return ConditionOutcome.match("always registered");
        }
    }

    static class Never implements Condition {
        @Override
        public ConditionOutcome matches(ConditionContext context, AnnotatedElement element) {
            return ConditionOutcome.noMatch("never registered");
        }
    }

    /** Shows what a condition may look at: the properties and the definitions registered so far. */
    static class WhenGreetingIsSet implements Condition {
        @Override
        public ConditionOutcome matches(ConditionContext context, AnnotatedElement element) {
            return context.environment().containsProperty("greeting")
                    ? ConditionOutcome.match("property 'greeting' is set, among %d definitions".formatted(context.registry().definitions().size()))
                    : ConditionOutcome.noMatch("property 'greeting' is not set");
        }
    }

    static class NeedsArguments implements Condition {
        NeedsArguments(String unused) {
        }

        @Override
        public ConditionOutcome matches(ConditionContext context, AnnotatedElement element) {
            return ConditionOutcome.match("never reached");
        }
    }

    @Conditional(Always.class)
    static class Accepted {
    }

    @Conditional(Never.class)
    static class Rejected {
    }

    @Conditional({Always.class, Never.class})
    static class AcceptedThenRejected {
    }

    @Conditional(NeedsArguments.class)
    static class Impossible {
    }

    @Configuration
    @Conditional(Never.class)
    static class RejectedConfig {
        @Bean
        String hidden() {
            return "hidden";
        }
    }

    @Configuration
    static class PartialConfig {
        @Bean
        @Conditional(Always.class)
        String kept() {
            return "kept";
        }

        @Bean
        @Conditional(Never.class)
        Integer dropped() {
            return 1;
        }
    }

    @Conditional(WhenGreetingIsSet.class)
    static class Greeter {
    }

    @Test
    void registersABeanWhoseConditionMatches() {
        try (var context = AnnotationApplicationContext.of(Accepted.class)) {
            assertThat(context.containsBean("accepted")).isTrue();
        }
    }

    @Test
    void skipsABeanWhoseConditionDoesNotMatch() {
        try (var context = AnnotationApplicationContext.of(Rejected.class)) {
            assertThat(context.containsBean("rejected")).isFalse();
            assertThatThrownBy(() -> context.getBean(Rejected.class)).isInstanceOf(NoSuchBeanException.class);
        }
    }

    @Test
    void everyConditionMustMatch() {
        try (var context = AnnotationApplicationContext.of(AcceptedThenRejected.class)) {
            assertThat(context.containsBean("acceptedThenRejected")).isFalse();
        }
    }

    @Test
    void aRejectedConfigurationTakesItsBeanMethodsWithIt() {
        try (var context = AnnotationApplicationContext.of(RejectedConfig.class)) {
            assertThat(context.containsBean("rejectedConfig")).isFalse();
            assertThat(context.containsBean("hidden")).isFalse();
        }
    }

    @Test
    void aBeanMethodCanBeSkippedOnItsOwn() {
        try (var context = AnnotationApplicationContext.of(PartialConfig.class)) {
            assertThat(context.containsBean("kept")).isTrue();
            assertThat(context.containsBean("dropped")).isFalse();
        }
    }

    @Test
    void aConditionSeesThePropertiesAndTheDefinitionsRegisteredSoFar() {
        try (var withProperty = AnnotationApplicationContext.builder()
                .environment(new StandardEnvironment(Map.of("greeting", "hello")))
                .register(Greeter.class)
                .build()) {
            assertThat(withProperty.containsBean("greeter")).isTrue();
        }
        try (var without = AnnotationApplicationContext.builder()
                .environment(new StandardEnvironment(Map.of()))
                .register(Greeter.class)
                .build()) {
            assertThat(without.containsBean("greeter")).isFalse();
        }
    }

    @Test
    void explainsThatAConditionNeedsAConstructorWithoutArguments() {
        assertThatThrownBy(() -> AnnotationApplicationContext.of(Impossible.class))
                .isInstanceOf(BeanDefinitionException.class)
                .hasMessageContaining("needs a constructor without arguments");
    }
}
