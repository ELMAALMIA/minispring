package io.minispring.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.minispring.autoconfigure.fixtures.Clock;
import io.minispring.autoconfigure.fixtures.ClockAutoConfiguration;
import io.minispring.autoconfigure.fixtures.ReportAutoConfiguration;
import io.minispring.container.annotation.Component;
import io.minispring.container.context.AnnotationApplicationContext;
import io.minispring.container.env.StandardEnvironment;
import io.minispring.container.exception.BeanDefinitionException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AutoConfigurationRegistrarTest {

    @Component
    static class ApplicationClock implements Clock {
        @Override
        public String now() {
            return "application clock";
        }
    }

    /** Claims to belong both after and before the same class, which cannot be satisfied. */
    @AutoConfigureAfter(SecondInCycle.class)
    @AutoConfigureBefore(SecondInCycle.class)
    static class FirstInCycle {
    }

    static class SecondInCycle {
    }

    @AutoConfigureBefore(Late.class)
    static class BeforeLate {
    }

    @AutoConfigureOrder(10)
    static class Late {
    }

    @AutoConfigureOrder(-10)
    static class Early {
    }

    @Test
    void contributesTheAutoConfigurationsListedOnTheClasspath() {
        try (var context = contextWith(Map.of())) {
            assertThat(context.getBean(Clock.class).now()).isEqualTo("default clock");
            assertThat(context.getBean(String.class)).isEqualTo("report of default clock");
        }
    }

    @Test
    void theApplicationBeanWinsOverTheAutoConfiguredOne() {
        try (var context = AnnotationApplicationContext.builder()
                .register(ApplicationClock.class)
                .apply(new AutoConfigurationRegistrar())
                .build()) {
            assertThat(context.getBean(Clock.class).now()).isEqualTo("application clock");
            assertThat(context.getBean(String.class)).isEqualTo("report of application clock");
        }
    }

    @Test
    void aPropertyCanTurnAnAutoConfigurationOff() {
        try (var context = contextWith(Map.of("fixtures.clock.enabled", "false"))) {
            assertThat(context.containsBean("clock")).isFalse();
            // The report needs a clock, so it disappears with it.
            assertThat(context.containsBean("clockReport")).isFalse();
        }
    }

    @Test
    void readsTheImportsFileIgnoringCommentsAndBlankLines() {
        List<Class<?>> imported = AutoConfigurationImports.load(Thread.currentThread().getContextClassLoader());

        assertThat(imported).containsExactly(ClockAutoConfiguration.class, ReportAutoConfiguration.class);
    }

    @Test
    void ordersByDeclaredOrderThenByConstraints() {
        assertThat(AutoConfigurationSorter.sort(List.of(Late.class, Early.class))).containsExactly(Early.class, Late.class);
        assertThat(AutoConfigurationSorter.sort(List.of(ReportAutoConfiguration.class, ClockAutoConfiguration.class)))
                .containsExactly(ClockAutoConfiguration.class, ReportAutoConfiguration.class);
    }

    @Test
    void aConstraintWinsOverTheDeclaredOrder() {
        assertThat(AutoConfigurationSorter.sort(List.of(Late.class, BeforeLate.class)))
                .containsExactly(BeforeLate.class, Late.class);
    }

    @Test
    void refusesAnOrderingCycleAndNamesTheClassesInvolved() {
        List<Class<?>> cycle = List.of(FirstInCycle.class, SecondInCycle.class);

        assertThatThrownBy(() -> AutoConfigurationSorter.sort(cycle))
                .isInstanceOf(BeanDefinitionException.class)
                .hasMessageContaining("form a cycle between")
                .hasMessageContaining("FirstInCycle")
                .hasMessageContaining("SecondInCycle");
    }

    private static AnnotationApplicationContext contextWith(Map<String, String> properties) {
        return AnnotationApplicationContext.builder()
                .environment(new StandardEnvironment(properties))
                .apply(new AutoConfigurationRegistrar())
                .build();
    }
}
