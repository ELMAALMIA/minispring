package io.minispring.container.web;

import io.minispring.container.annotation.Component;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a bean whose {@link GetMapping} methods answer HTTP requests. The value a handler
 * returns is written as the response body.
 *
 * <p>It is itself annotated with {@link Component}, so the scanner treats a controller as any
 * other bean. That is all a "stereotype" annotation is.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface RestController {
}
