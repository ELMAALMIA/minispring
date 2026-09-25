package io.minispring.autoconfigure.condition;

import io.minispring.container.condition.Conditional;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Registers the bean only if none of the named classes is on the classpath. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(OnMissingClassCondition.class)
public @interface ConditionalOnMissingClass {

    String[] name();
}
