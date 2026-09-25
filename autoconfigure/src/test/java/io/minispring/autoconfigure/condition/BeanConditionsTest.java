package io.minispring.autoconfigure.condition;

import static org.assertj.core.api.Assertions.assertThat;

import io.minispring.container.annotation.Bean;
import io.minispring.container.annotation.Component;
import io.minispring.container.annotation.Configuration;
import io.minispring.container.condition.Conditional;
import io.minispring.container.context.AnnotationApplicationContext;
import org.junit.jupiter.api.Test;

class BeanConditionsTest {

    interface Clock {
        String now();
    }

    interface Cache {
    }

    @Component
    static class ApplicationClock implements Clock {
        @Override
        public String now() {
            return "from the application";
        }
    }

    @Configuration
    static class DefaultsConfiguration {

        @Bean
        @ConditionalOnMissingBean
        Clock clock() {
            return () -> "default";
        }

        @Bean
        @ConditionalOnBean(Clock.class)
        String clockReport() {
            return "a clock is available";
        }

        @Bean
        @ConditionalOnBean(Cache.class)
        Integer cacheSize() {
            return 42;
        }
    }

    @ConditionalOnMissingBean(Clock.class)
    static class FallbackClockHolder {
    }

    @Conditional({OnBeanCondition.class, OnMissingBeanCondition.class})
    static class WithoutTheMatchingAnnotations {
    }

    @Test
    void contributesTheDefaultWhenNoBeanOfThatTypeExists() {
        try (var context = AnnotationApplicationContext.of(DefaultsConfiguration.class)) {
            assertThat(context.getBean(Clock.class).now()).isEqualTo("default");
        }
    }

    @Test
    void standsAsideWhenABeanOfThatTypeIsAlreadyRegistered() {
        try (var context = AnnotationApplicationContext.of(ApplicationClock.class, DefaultsConfiguration.class)) {
            assertThat(context.getBean(Clock.class).now()).isEqualTo("from the application");
            assertThat(context.containsBean("clock")).isFalse();
        }
    }

    @Test
    void requiresThatTheOtherBeanExists() {
        try (var context = AnnotationApplicationContext.of(DefaultsConfiguration.class)) {
            assertThat(context.containsBean("clockReport")).isTrue();
            assertThat(context.containsBean("cacheSize")).isFalse();
        }
    }

    @Test
    void looksForTheDeclaredTypeOnAClass() {
        try (var alone = AnnotationApplicationContext.of(FallbackClockHolder.class)) {
            assertThat(alone.containsBean("fallbackClockHolder")).isTrue();
        }
        try (var withClock = AnnotationApplicationContext.of(ApplicationClock.class, FallbackClockHolder.class)) {
            assertThat(withClock.containsBean("fallbackClockHolder")).isFalse();
        }
    }

    /** The decision depends on what is registered at that moment, which is why it belongs in an auto-configuration. */
    @Test
    void theAnswerDependsOnTheRegistrationOrder() {
        try (var defaultsFirst = AnnotationApplicationContext.of(DefaultsConfiguration.class, ApplicationClock.class)) {
            assertThat(defaultsFirst.containsBean("clock")).isTrue();
        }
        try (var applicationFirst = AnnotationApplicationContext.of(ApplicationClock.class, DefaultsConfiguration.class)) {
            assertThat(applicationFirst.containsBean("clock")).isFalse();
        }
    }

    @Test
    void aConditionUsedWithoutItsAnnotationBlocksNothing() {
        try (var context = AnnotationApplicationContext.of(WithoutTheMatchingAnnotations.class)) {
            assertThat(context.containsBean("withoutTheMatchingAnnotations")).isTrue();
        }
    }
}
