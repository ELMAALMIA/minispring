package io.minispring.autoconfigure.condition;

import io.minispring.container.condition.Conditional;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers the bean only if a bean of every given type is already registered, which is how an
 * auto-configuration completes what the application started.
 *
 * <p>Like {@link ConditionalOnMissingBean}, it reads the definitions registered so far, so it
 * belongs in an auto-configuration rather than in application code.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(OnBeanCondition.class)
public @interface ConditionalOnBean {

    /** The types to look for. Left empty, the annotated class or the method's return type is used. */
    Class<?>[] value() default {};
}
