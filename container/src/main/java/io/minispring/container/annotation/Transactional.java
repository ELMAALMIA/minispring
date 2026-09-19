package io.minispring.container.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runs a method inside a transaction. The method must be declared by an interface of the bean,
 * because the transaction is opened by a JDK dynamic proxy that implements those interfaces.
 *
 * <p>As in Spring, a {@link RuntimeException} or an {@link Error} rolls the transaction back,
 * and a checked exception commits it.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Transactional {
}
