package io.minispring.autoconfigure.condition;

import static org.assertj.core.api.Assertions.assertThat;

import io.minispring.container.condition.Conditional;
import io.minispring.container.context.AnnotationApplicationContext;
import io.minispring.container.env.StandardEnvironment;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ClassAndPropertyConditionsTest {

    private static final String PRESENT = "java.sql.DriverManager";
    private static final String ABSENT = "com.example.NotOnTheClasspath";

    @ConditionalOnClass(name = PRESENT)
    static class NeedsAvailableClass {
    }

    @ConditionalOnClass(name = {PRESENT, ABSENT})
    static class NeedsMissingClass {
    }

    @ConditionalOnMissingClass(name = ABSENT)
    static class WhenClassIsAbsent {
    }

    @ConditionalOnMissingClass(name = PRESENT)
    static class WhenClassIsPresent {
    }

    @ConditionalOnProperty(name = "minispring.audit.enabled")
    static class EnabledByProperty {
    }

    @ConditionalOnProperty(name = "minispring.audit.enabled", matchIfMissing = true)
    static class EnabledUnlessTurnedOff {
    }

    @ConditionalOnProperty(name = "minispring.audit.mode", havingValue = "verbose")
    static class VerboseOnly {
    }

    /** Each condition is also usable through plain @Conditional, where it has nothing to check. */
    @Conditional({OnClassCondition.class, OnMissingClassCondition.class, OnPropertyCondition.class})
    static class WithoutTheMatchingAnnotations {
    }

    @Test
    void aConditionUsedWithoutItsAnnotationBlocksNothing() {
        try (var context = contextWith(Map.of(), WithoutTheMatchingAnnotations.class)) {
            assertThat(context.containsBean("withoutTheMatchingAnnotations")).isTrue();
        }
    }

    @Test
    void registersWhenTheRequiredClassIsOnTheClasspath() {
        try (var context = contextWith(Map.of(), NeedsAvailableClass.class, NeedsMissingClass.class)) {
            assertThat(context.containsBean("needsAvailableClass")).isTrue();
            assertThat(context.containsBean("needsMissingClass")).isFalse();
        }
    }

    @Test
    void registersWhenTheUnwantedClassIsAbsent() {
        try (var context = contextWith(Map.of(), WhenClassIsAbsent.class, WhenClassIsPresent.class)) {
            assertThat(context.containsBean("whenClassIsAbsent")).isTrue();
            assertThat(context.containsBean("whenClassIsPresent")).isFalse();
        }
    }

    @Test
    void readsThePropertyThatSwitchesABeanOn() {
        try (var enabled = contextWith(Map.of("minispring.audit.enabled", "true"), EnabledByProperty.class)) {
            assertThat(enabled.containsBean("enabledByProperty")).isTrue();
        }
        try (var disabled = contextWith(Map.of("minispring.audit.enabled", "false"), EnabledByProperty.class)) {
            assertThat(disabled.containsBean("enabledByProperty")).isFalse();
        }
        try (var unset = contextWith(Map.of(), EnabledByProperty.class)) {
            assertThat(unset.containsBean("enabledByProperty")).isFalse();
        }
    }

    @Test
    void anAbsentPropertyCanStillCountAsAMatch() {
        try (var unset = contextWith(Map.of(), EnabledUnlessTurnedOff.class)) {
            assertThat(unset.containsBean("enabledUnlessTurnedOff")).isTrue();
        }
        try (var turnedOff = contextWith(Map.of("minispring.audit.enabled", "false"), EnabledUnlessTurnedOff.class)) {
            assertThat(turnedOff.containsBean("enabledUnlessTurnedOff")).isFalse();
        }
    }

    @Test
    void comparesThePropertyWithTheExpectedValueIgnoringCase() {
        try (var matching = contextWith(Map.of("minispring.audit.mode", "VERBOSE"), VerboseOnly.class)) {
            assertThat(matching.containsBean("verboseOnly")).isTrue();
        }
        try (var other = contextWith(Map.of("minispring.audit.mode", "quiet"), VerboseOnly.class)) {
            assertThat(other.containsBean("verboseOnly")).isFalse();
        }
    }

    private static AnnotationApplicationContext contextWith(Map<String, String> properties, Class<?>... classes) {
        return AnnotationApplicationContext.builder()
                .environment(new StandardEnvironment(properties))
                .register(classes)
                .build();
    }
}
