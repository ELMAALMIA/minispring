package io.minispring.container.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Makes a bean the default choice when several beans match an injection point that has no
 * {@link Qualifier}. It goes on the class of a scanned bean, or on its {@link Bean} method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Primary {
}
