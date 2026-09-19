package io.minispring.container.context;

import static org.assertj.core.api.Assertions.assertThat;

import io.minispring.fixtures.scan.Alpha;
import io.minispring.fixtures.scan.nested.Gamma;
import org.junit.jupiter.api.Test;

class BuilderTest {

    static class Clock {
    }

    @Test
    void combinesScannedPackagesWithExplicitlyRegisteredClasses() {
        try (var context = AnnotationApplicationContext.builder()
                .scan("io.minispring.fixtures.scan.nested")
                .register(Clock.class)
                .build()) {
            assertThat(context.getBean(Gamma.class)).isNotNull();
            assertThat(context.getBean(Clock.class)).isNotNull();
        }
    }

    @Test
    void registersAClassOnceEvenWhenItIsAlsoScanned() {
        try (var context = AnnotationApplicationContext.builder()
                .scan("io.minispring.fixtures.scan")
                .register(Alpha.class)
                .build()) {
            assertThat(context.containsBean("alpha")).isTrue();
        }
    }
}
