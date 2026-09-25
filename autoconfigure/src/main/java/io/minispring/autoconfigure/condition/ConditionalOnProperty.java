package io.minispring.autoconfigure.condition;

import io.minispring.container.condition.Conditional;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers the bean depending on a property, which is how a starter is switched off without
 * touching any code.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(OnPropertyCondition.class)
public @interface ConditionalOnProperty {

    /** The property to read, for example {@code "minispring.audit.enabled"}. */
    String name();

    /** The value the property must have, compared ignoring case. */
    String havingValue() default "true";

    /** Whether an absent property counts as a match. */
    boolean matchIfMissing() default false;
}
