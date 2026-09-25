package io.minispring.autoconfigure.condition;

import io.minispring.container.condition.Conditional;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers the bean only if no bean of the given types is registered yet. This is what "your
 * bean wins over the default one" is made of.
 *
 * <p><b>The answer depends on when the question is asked.</b> Conditions read the definitions
 * registered so far, so this annotation only behaves as expected inside an auto-configuration,
 * which runs after the application has declared everything it wants. Spring carries exactly the
 * same warning.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(OnMissingBeanCondition.class)
public @interface ConditionalOnMissingBean {

    /** The types to look for. Left empty, the annotated class or the method's return type is used. */
    Class<?>[] value() default {};
}
