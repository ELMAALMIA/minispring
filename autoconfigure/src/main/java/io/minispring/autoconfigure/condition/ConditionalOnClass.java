package io.minispring.autoconfigure.condition;

import io.minispring.container.condition.Conditional;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers the bean only if every named class is on the classpath. This is how a starter stays
 * silent until the library it configures is actually there.
 *
 * <p>Classes are named as strings on purpose. Reading a {@code Class} attribute that points at
 * an absent class fails, which is exactly the case this annotation exists for. Spring solves it
 * by reading the bytecode with ASM instead of reflection; this container simply asks for a name.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(OnClassCondition.class)
public @interface ConditionalOnClass {

    /** Fully qualified class names, for example {@code "java.sql.DriverManager"}. */
    String[] name();
}
