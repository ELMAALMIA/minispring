package io.minispring.container.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers the annotated bean only if every listed {@link Condition} matches.
 *
 * <p>It goes on a class or on a {@code @Bean} method. On a {@code @Configuration} class that
 * does not match, the class and all of its {@code @Bean} methods are skipped together.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Conditional {

    Class<? extends Condition>[] value();
}
