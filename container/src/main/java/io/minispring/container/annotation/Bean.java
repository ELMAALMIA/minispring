package io.minispring.container.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method of a {@link Configuration} class as the factory of a bean. The parameters of
 * the method are injected like constructor parameters.
 *
 * <p><b>Ask for your dependencies as parameters</b>, and never by calling another {@code @Bean}
 * method: such a call is a plain Java call, so it builds a second, unmanaged instance. Spring
 * hides this by generating a CGLIB subclass of the configuration class, which this container
 * deliberately does not do.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Bean {

    /** The bean name. When empty, the method name is used. */
    String value() default "";
}
