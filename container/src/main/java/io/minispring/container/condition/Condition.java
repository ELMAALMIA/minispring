package io.minispring.container.condition;

import java.lang.reflect.AnnotatedElement;

/**
 * Decides whether a bean should be registered at all.
 *
 * <p>Implementations need a constructor without arguments: a condition runs before the
 * container can create anything, so it cannot itself be injected.
 */
@FunctionalInterface
public interface Condition {

    /**
     * @param element the annotated class or {@code @Bean} method being considered
     */
    ConditionOutcome matches(ConditionContext context, AnnotatedElement element);
}
