package io.minispring.container.bean;

import static java.util.stream.Collectors.joining;

import io.minispring.container.annotation.Autowired;
import io.minispring.container.annotation.Component;
import io.minispring.container.annotation.PostConstruct;
import io.minispring.container.annotation.PreDestroy;
import io.minispring.container.annotation.Scope;
import io.minispring.container.annotation.ScopeType;
import io.minispring.container.exception.BeanDefinitionException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Turns a class into a {@link BeanDefinition}. It applies the naming, constructor, scope and
 * lifecycle rules, and rejects invalid classes before any bean is created.
 */
public final class BeanDefinitionReader {

    public BeanDefinition read(Class<?> type) {
        return new BeanDefinition(
                nameOf(type),
                type,
                constructorOf(type),
                scopeOf(type),
                lifecycleMethod(type, PostConstruct.class),
                lifecycleMethod(type, PreDestroy.class));
    }

    private static String nameOf(Class<?> type) {
        Component component = type.getAnnotation(Component.class);
        if (component != null && !component.value().isBlank()) {
            return component.value();
        }
        return decapitalize(type.getSimpleName());
    }

    /**
     * Applies the JavaBeans rule that Spring also uses: {@code OrderService} becomes
     * {@code orderService}, but a name starting with an acronym such as {@code URLParser} is kept.
     */
    private static String decapitalize(String name) {
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Uses the only constructor if there is one. Otherwise uses the one annotated with
     * {@link Autowired}. Guessing between several constructors would hide a design mistake.
     */
    private static Constructor<?> constructorOf(Class<?> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        if (constructors.length == 1) {
            return constructors[0];
        }
        List<Constructor<?>> autowired = Arrays.stream(constructors)
                .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
                .toList();
        if (autowired.size() == 1) {
            return autowired.getFirst();
        }
        String problem = autowired.isEmpty()
                ? "none is annotated with @Autowired"
                : autowired.size() + " are annotated with @Autowired";
        throw new BeanDefinitionException("Cannot choose a constructor for %s: it declares %d constructors and %s. Candidates: %s"
                .formatted(type.getName(), constructors.length, problem,
                        Arrays.stream(constructors).map(BeanDefinitionReader::signature).collect(joining(", "))));
    }

    private static ScopeType scopeOf(Class<?> type) {
        Scope scope = type.getAnnotation(Scope.class);
        return scope == null ? ScopeType.SINGLETON : scope.value();
    }

    /**
     * Finds the method annotated with {@code marker}. Only methods declared by the class itself
     * are considered: inherited callbacks are out of scope.
     */
    private static Optional<Method> lifecycleMethod(Class<?> type, Class<? extends Annotation> marker) {
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

    private static String signature(Constructor<?> constructor) {
        return constructor.getDeclaringClass().getSimpleName() + Arrays.stream(constructor.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(joining(", ", "(", ")"));
    }
}
