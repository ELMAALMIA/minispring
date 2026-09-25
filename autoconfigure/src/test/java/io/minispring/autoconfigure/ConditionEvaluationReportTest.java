package io.minispring.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.minispring.autoconfigure.ConditionEvaluationReport.Decision;
import io.minispring.autoconfigure.fixtures.Clock;
import io.minispring.container.annotation.Component;
import io.minispring.container.context.AnnotationApplicationContext;
import io.minispring.container.env.StandardEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConditionEvaluationReportTest {

    @Component
    static class ApplicationClock implements Clock {
        @Override
        public String now() {
            return "application clock";
        }
    }

    @Test
    void recordsWhyEachAutoConfigurationWasKept() {
        AutoConfigurationRegistrar registrar = new AutoConfigurationRegistrar();

        try (var context = AnnotationApplicationContext.builder().apply(registrar).build()) {
            assertThat(context.containsBean("clock")).isTrue();
        }

        assertThat(registrar.report().positiveMatches())
                .extracting(Decision::name)
                .contains("ClockAutoConfiguration", "ClockAutoConfiguration#clock");
        assertThat(registrar.report().negativeMatches()).isEmpty();
    }

    @Test
    void recordsWhyABeanWasSkippedAndNamesTheBeanThatWon() {
        AutoConfigurationRegistrar registrar = new AutoConfigurationRegistrar();

        try (var context = AnnotationApplicationContext.builder()
                .register(ApplicationClock.class)
                .apply(registrar)
                .build()) {
            assertThat(context.containsBean("clock")).isFalse();
        }

        assertThat(registrar.report().negativeMatches())
                .singleElement()
                .satisfies(decision -> {
                    assertThat(decision.name()).isEqualTo("ClockAutoConfiguration#clock");
                    assertThat(decision.reasons()).contains("bean applicationClock of type Clock is already registered");
                });
    }

    @Test
    void writesAReportThatReadsLikeSpringBootsDebugOutput() {
        AutoConfigurationRegistrar registrar = new AutoConfigurationRegistrar();

        try (var context = AnnotationApplicationContext.builder()
                .environment(new StandardEnvironment(Map.of("fixtures.clock.enabled", "false")))
                .apply(registrar)
                .build()) {
            assertThat(context.containsBean("clock")).isFalse();
        }

        assertThat(registrar.report().toText())
                .contains("CONDITION EVALUATION REPORT")
                .contains("Negative matches:")
                .contains("  ClockAutoConfiguration")
                .contains("    - property 'fixtures.clock.enabled' is 'false', expected 'true'")
                .contains("Positive matches:");
    }

    @Test
    void printsTheReportWhenThePropertyAsksForIt() {
        PrintStream original = System.out;
        var buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try (var context = AnnotationApplicationContext.builder()
                .environment(new StandardEnvironment(Map.of(AutoConfigurationRegistrar.REPORT_PROPERTY, "true")))
                .apply(new AutoConfigurationRegistrar())
                .build()) {
            assertThat(context).isNotNull();
        } finally {
            System.setOut(original);
        }

        assertThat(buffer.toString(StandardCharsets.UTF_8)).contains("CONDITION EVALUATION REPORT");
    }
}
