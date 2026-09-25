package io.minispring.container.env;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** One named place where properties come from, such as the system properties or a file. */
public interface PropertySource {

    String name();

    Optional<String> getProperty(String key);

    /** A fixed set of properties, for example the ones read from a file. */
    record OfMap(String name, Map<String, String> properties) implements PropertySource {

        public OfMap {
            Objects.requireNonNull(name, "name");
            properties = Map.copyOf(properties);
        }

        @Override
        public Optional<String> getProperty(String key) {
            return Optional.ofNullable(properties.get(key));
        }
    }

    /** The JVM system properties, read live so a test can set one. */
    record OfSystemProperties() implements PropertySource {

        @Override
        public String name() {
            return "systemProperties";
        }

        @Override
        public Optional<String> getProperty(String key) {
            return Optional.ofNullable(System.getProperty(key));
        }
    }

    /**
     * Environment variables, matched loosely: {@code minispring.audit.enabled} also reads
     * {@code MINISPRING_AUDIT_ENABLED}, because shells do not allow dots.
     */
    record OfEnvironmentVariables(Map<String, String> variables) implements PropertySource {

        public OfEnvironmentVariables {
            variables = Map.copyOf(variables);
        }

        public static OfEnvironmentVariables ofSystem() {
            return new OfEnvironmentVariables(System.getenv());
        }

        @Override
        public String name() {
            return "environmentVariables";
        }

        @Override
        public Optional<String> getProperty(String key) {
            return Optional.ofNullable(variables.get(key))
                    .or(() -> Optional.ofNullable(variables.get(key.replace('.', '_').toUpperCase(java.util.Locale.ROOT))));
        }
    }
}
