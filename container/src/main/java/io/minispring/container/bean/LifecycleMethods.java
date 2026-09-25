package io.minispring.container.bean;

import io.minispring.container.annotation.PostConstruct;
import io.minispring.container.annotation.PreDestroy;
import io.minispring.container.exception.BeanDefinitionException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@link PostConstruct} and {@link PreDestroy} methods of a class, found and validated once.
 *
 * <p>Only methods declared by the class itself are considered: inherited callbacks are out of scope.
 */
public record LifecycleMethods(Optional<Method> postConstruct, Optional<Method> preDestroy) {

    public LifecycleMethods {
        Objects.requireNonNull(postConstruct, "postConstruct");
        Objects.requireNonNull(preDestroy, "preDestroy");
    }

    public static LifecycleMethods of(Class<?> type) {
        return new LifecycleMethods(find(type, PostConstruct.class), find(type, PreDestroy.class));
    }

    private static Optional<Method> find(Class<?> type, Class<? extends Annotation> marker) {
        List<Method> methods = Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(marker))
                .toList();
        if (methods.isEmpty()) {
            return Optional.empty();
        }
        if (methods.size() > 1) {
            throw new BeanDefinitionException("%s declares %d @%s methods; at most one is allowed."
                    .formatted(type.getName(), methods.size(), marker.getSimpleName()));
        }
        Method method = methods.getFirst();
        if (method.getParameterCount() > 0) {
            throw new BeanDefinitionException("@%s method %s.%s must not take parameters."
                    .formatted(marker.getSimpleName(), type.getName(), method.getName()));
        }
        return Optional.of(method);
    }
}
