package io.minispring.container.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.minispring.container.annotation.Bean;
import io.minispring.container.annotation.Component;
import io.minispring.container.annotation.Configuration;
import io.minispring.container.annotation.PostConstruct;
import io.minispring.container.annotation.Primary;
import io.minispring.container.annotation.Scope;
import io.minispring.container.annotation.ScopeType;
import io.minispring.container.exception.BeanCreationException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigurationClassTest {

    static final List<String> EVENTS = new ArrayList<>();

    interface Clock {
        String now();
    }

    static class FixedClock implements Clock {
        private final String value;

        FixedClock(String value) {
            this.value = value;
        }

        @Override
        public String now() {
            return value;
        }

        @PostConstruct
        void started() {
            EVENTS.add("clock ready");
        }
    }

    @Component
    static class Ticker {
    }

    record Report(Clock clock, Ticker ticker) {
    }

    @Configuration
    static class AppConfig {

        @Bean
        Clock clock() {
            return new FixedClock("12:00");
        }

        @Bean("dailyReport")
        Report report(Clock clock, Ticker ticker) {
            return new Report(clock, ticker);
        }

        @Bean
        @Scope(ScopeType.PROTOTYPE)
        StringBuilder buffer() {
            return new StringBuilder();
        }
    }

    @Configuration
    static class PrimaryConfig {

        @Bean
        @Primary
        Clock preferredClock() {
            return new FixedClock("primary");
        }

        @Bean
        Clock otherClock() {
            return new FixedClock("other");
        }
    }

    @Configuration
    static class SelfCallingConfig {

        @Bean
        Clock clock() {
            return new FixedClock("12:00");
        }

        @Bean
        Report report(Ticker ticker) {
            // A plain Java call: it builds an instance the container knows nothing about.
            return new Report(clock(), ticker);
        }
    }

    @Configuration
    static class FailingConfig {

        @Bean
        Clock clock() {
            throw new IllegalStateException("no clock today");
        }
    }

    @BeforeEach
    void clearEvents() {
        EVENTS.clear();
    }

    @Test
    void createsABeanFromAFactoryMethodNamedAfterTheMethod() {
        try (var context = AnnotationApplicationContext.of(AppConfig.class, Ticker.class)) {
            assertThat(context.containsBean("clock")).isTrue();
            assertThat(context.getBean(Clock.class).now()).isEqualTo("12:00");
        }
    }

    @Test
    void injectsTheParametersOfAFactoryMethodAndHonorsAnExplicitName() {
        try (var context = AnnotationApplicationContext.of(AppConfig.class, Ticker.class)) {
            Report report = context.getBean("dailyReport", Report.class);

            assertThat(report.clock()).isSameAs(context.getBean(Clock.class));
            assertThat(report.ticker()).isSameAs(context.getBean(Ticker.class));
        }
    }

    @Test
    void appliesTheScopeDeclaredOnTheFactoryMethod() {
        try (var context = AnnotationApplicationContext.of(AppConfig.class, Ticker.class)) {
            assertThat(context.getBean(StringBuilder.class)).isNotSameAs(context.getBean(StringBuilder.class));
        }
    }

    @Test
    void runsTheLifecycleCallbacksOfTheReturnedType() {
        try (var context = AnnotationApplicationContext.of(AppConfig.class, Ticker.class)) {
            context.getBean(Clock.class);

            assertThat(EVENTS).containsExactly("clock ready");
        }
    }

    @Test
    void aFactoryMethodCanBeMarkedPrimary() {
        try (var context = AnnotationApplicationContext.of(PrimaryConfig.class)) {
            assertThat(context.getBean(Clock.class).now()).isEqualTo("primary");
        }
    }

    @Test
    void callingAnotherBeanMethodBuildsASecondUnmanagedInstance() {
        try (var context = AnnotationApplicationContext.of(SelfCallingConfig.class, Ticker.class)) {
            Report report = context.getBean(Report.class);

            // Spring hides this with a CGLIB subclass of the configuration class; this container does not.
            assertThat(report.clock()).isNotSameAs(context.getBean(Clock.class));
        }
    }

    @Test
    void reportsAFailingFactoryMethodWithTheBeanName() {
        assertThatThrownBy(() -> AnnotationApplicationContext.of(FailingConfig.class))
                .isInstanceOf(BeanCreationException.class)
                .hasMessageContaining("Cannot create bean 'clock'")
                .hasMessageContaining("@Bean method threw an exception")
                .hasRootCauseInstanceOf(IllegalStateException.class);
    }
}
