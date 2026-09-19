package io.minispring.container.bean;

import static java.util.stream.Collectors.joining;

import io.minispring.container.annotation.Autowired;
import io.minispring.container.annotation.Component;
import io.minispring.container.annotation.Scope;
import io.minispring.container.annotation.ScopeType;
import io.minispring.container.exception.BeanDefinitionException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

/** Turns a class into a {@link BeanDefinition} by applying the naming and constructor rules. */
public final class BeanDefinitionReader {

    public BeanDefinition read(Class<?> type) {
        return new BeanDefinition(nameOf(type), type, constructorOf(type), scopeOf(type));
    }

    private static ScopeType scopeOf(Class<?> type) {
        Scope scope = type.getAnnotation(Scope.class);
        return scope == null ? ScopeType.SINGLETON : scope.value();
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

    private static String signature(Constructor<?> constructor) {
        return constructor.getDeclaringClass().getSimpleName() + Arrays.stream(constructor.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(joining(", ", "(", ")"));
    }
}
