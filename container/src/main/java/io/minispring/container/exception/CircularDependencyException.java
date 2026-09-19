package io.minispring.container.exception;

import java.util.List;

/**
 * Thrown when beans depend on each other in a loop.
 *
 * <p>With constructor injection, a cycle cannot be resolved: A cannot be constructed before B
 * if each one needs the other. Spring only resolves cycles for field injection, by exposing
 * half-built objects early. This container refuses instead.
 */
public final class CircularDependencyException extends ContainerException {

    public CircularDependencyException(List<String> cycle) {
        super("Circular dependency: %s. Constructor injection cannot create a bean before its own dependencies exist; move the shared logic into a separate bean to break the cycle."
                .formatted(String.join(" -> ", cycle)));
    }
}
