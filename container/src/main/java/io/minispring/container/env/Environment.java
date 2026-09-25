package io.minispring.container.env;

import java.util.Optional;

/**
 * The properties an application is configured with. Beans can have one injected, and conditions
 * use it to decide whether a bean should exist at all.
 */
public interface Environment {

    Optional<String> getProperty(String name);

    default String getProperty(String name, String defaultValue) {
        return getProperty(name).orElse(defaultValue);
    }

    default boolean containsProperty(String name) {
        return getProperty(name).isPresent();
    }
}
