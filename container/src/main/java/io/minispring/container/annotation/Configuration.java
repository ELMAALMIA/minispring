package io.minispring.container.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class whose {@link Bean} methods define beans. The class is itself a bean, because it
 * is meta-annotated with {@link Component}.
 *
 * <p>Use it when the container cannot create a type by itself: a class from a library, or an
 * object that needs assembling.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface Configuration {
}
