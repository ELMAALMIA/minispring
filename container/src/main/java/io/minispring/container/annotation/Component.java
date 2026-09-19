package io.minispring.container.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a bean that the container should create and manage.
 *
 * <p>Annotations are inert metadata. {@code RUNTIME} retention is what keeps this one in the
 * class file at run time. The default, {@code CLASS}, would make it invisible to reflection,
 * and the scanner would silently find nothing.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {

    /** The bean name. When empty, it is derived from the class name: {@code OrderService} becomes {@code orderService}. */
    String value() default "";
}
