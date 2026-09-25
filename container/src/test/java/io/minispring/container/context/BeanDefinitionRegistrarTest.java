package io.minispring.container.context;

import static org.assertj.core.api.Assertions.assertThat;

import io.minispring.container.annotation.Bean;
import io.minispring.container.annotation.Component;
import io.minispring.container.annotation.Configuration;
import io.minispring.container.condition.Condition;
import io.minispring.container.condition.ConditionContext;
import io.minispring.container.condition.ConditionOutcome;
import io.minispring.container.condition.Conditional;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BeanDefinitionRegistrarTest {

    interface Clock {
        String now();
    }

    @Component
    static class ApplicationClock implements Clock {
        @Override
        public String now() {
            return "from the application";
        }
    }

    static class WhenNoClockIsRegistered implements Condition {
        @Override
        public ConditionOutcome matches(ConditionContext context, AnnotatedElement element) {
            return context.registry().definitionsOfType(Clock.class).isEmpty()
                    ? ConditionOutcome.match("no Clock is registered yet")
                    : ConditionOutcome.noMatch("a Clock is already registered");
        }
    }

    @Configuration
    static class DefaultClockConfiguration {
        @Bean
        @Conditional(WhenNoClockIsRegistered.class)
        Clock clock() {
            return () -> "from the registrar";
        }
    }

    /** A registrar that offers defaults, the way an auto-configuration does. */
    static class DefaultsRegistrar implements BeanDefinitionRegistrar {
        final List<String> beansItCouldSee = new ArrayList<>();

        @Override
        public void registerDefinitions(RegistrationContext context) {
            context.registry().definitions().forEach(definition -> beansItCouldSee.add(definition.name()));
            context.registerBeans(DefaultClockConfiguration.class);
        }
    }

    @Test
    void contributesABeanWhenTheApplicationHasNone() {
        DefaultsRegistrar registrar = new DefaultsRegistrar();

        try (var context = AnnotationApplicationContext.builder().apply(registrar).build()) {
            assertThat(context.getBean(Clock.class).now()).isEqualTo("from the registrar");
        }
    }

    @Test
    void standsAsideWhenTheApplicationDeclaredItsOwn() {
        DefaultsRegistrar registrar = new DefaultsRegistrar();

        try (var context = AnnotationApplicationContext.builder()
                .register(ApplicationClock.class)
                .apply(registrar)
                .build()) {
            assertThat(context.getBean(Clock.class).now()).isEqualTo("from the application");
            assertThat(context.containsBean("clock")).isFalse();
        }
    }

    @Test
    void runsAfterTheClassesRegisteredByTheApplication() {
        DefaultsRegistrar registrar = new DefaultsRegistrar();

        try (var context = AnnotationApplicationContext.builder()
                .register(ApplicationClock.class)
                .apply(registrar)
                .build()) {
            assertThat(context).isNotNull();
        }

        assertThat(registrar.beansItCouldSee).containsExactly("applicationClock");
    }
}
