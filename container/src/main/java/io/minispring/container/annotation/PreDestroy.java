package io.minispring.container.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a no-argument method that runs when the context closes. Singletons are destroyed in
 * reverse creation order, so a bean is destroyed before the beans it depends on. Prototypes
 * are never destroyed, because the container keeps no reference to them.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PreDestroy {
}
