package io.minispring.autoconfigure;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gives a rough position to an auto-configuration; the lowest value goes first. Use it for
 * preference, and {@link AutoConfigureAfter} for a real dependency.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoConfigureOrder {

    int value();
}
