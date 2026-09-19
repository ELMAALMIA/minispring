package io.minispring.container.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ComponentTest {

    @Component("custom")
    static class NamedComponent {
    }

    @Component
    static class UnnamedComponent {
    }

    @Test
    void isVisibleThroughReflectionAtRunTime() {
        assertThat(UnnamedComponent.class.isAnnotationPresent(Component.class)).isTrue();
    }

    @Test
    void carriesTheExplicitBeanNameWhenGiven() {
        assertThat(NamedComponent.class.getAnnotation(Component.class).value()).isEqualTo("custom");
        assertThat(UnnamedComponent.class.getAnnotation(Component.class).value()).isEmpty();
    }
}
