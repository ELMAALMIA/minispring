package io.minispring.container.condition;

import io.minispring.container.bean.BeanRegistry;
import io.minispring.container.env.Environment;
import java.util.Objects;

/**
 * What a condition is allowed to look at: the definitions registered <em>so far</em>, the
 * properties, and the class loader.
 *
 * <p>Conditions see definitions, never instances. Deciding whether a bean should exist must not
 * create beans, otherwise the question would answer itself.
 */
public record ConditionContext(BeanRegistry registry, Environment environment, ClassLoader classLoader) {

    public ConditionContext {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(classLoader, "classLoader");
    }
}
