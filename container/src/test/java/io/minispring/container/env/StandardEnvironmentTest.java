package io.minispring.container.env;

import static org.assertj.core.api.Assertions.assertThat;

import io.minispring.container.context.AnnotationApplicationContext;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StandardEnvironmentTest {

    static class Greeter {
        final Environment environment;

        Greeter(Environment environment) {
            this.environment = environment;
        }
    }

    @Test
    void readsAPropertyFromTheClasspathFile() {
        assertThat(new StandardEnvironment().getProperty("minispring.demo.greeting")).contains("hello from the file");
    }

    @Test
    void anOverrideWinsOverTheFile() {
        Environment environment = new StandardEnvironment(Map.of("minispring.demo.greeting", "overridden"));

        assertThat(environment.getProperty("minispring.demo.greeting")).contains("overridden");
    }

    @Test
    void systemPropertiesWinOverTheFile() {
        System.setProperty("minispring.demo.greeting", "from the system");
        try {
            assertThat(new StandardEnvironment().getProperty("minispring.demo.greeting")).contains("from the system");
        } finally {
            System.clearProperty("minispring.demo.greeting");
        }
    }

    @Test
    void environmentVariablesAlsoMatchTheirShellSpelling() {
        PropertySource variables = new PropertySource.OfEnvironmentVariables(Map.of("MINISPRING_AUDIT_ENABLED", "false"));

        assertThat(variables.getProperty("minispring.audit.enabled")).contains("false");
        assertThat(variables.getProperty("minispring.audit.missing")).isEmpty();
    }

    @Test
    void returnsTheDefaultWhenNothingDefinesTheProperty() {
        Environment environment = new StandardEnvironment(List.of(new PropertySource.OfMap("empty", Map.of())));

        assertThat(environment.getProperty("absent")).isEmpty();
        assertThat(environment.getProperty("absent", "fallback")).isEqualTo("fallback");
        assertThat(environment.containsProperty("absent")).isFalse();
    }

    @Test
    void beansCanHaveTheEnvironmentInjected() {
        Environment environment = new StandardEnvironment(Map.of("key", "value"));

        try (var context = AnnotationApplicationContext.builder().environment(environment).register(Greeter.class).build()) {
            assertThat(context.getBean(Greeter.class).environment).isSameAs(environment);
            assertThat(context.getEnvironment().getProperty("key")).contains("value");
        }
    }
}
