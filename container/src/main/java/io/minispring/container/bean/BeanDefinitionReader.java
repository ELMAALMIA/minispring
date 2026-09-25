package io.minispring.container.bean;

import static java.util.stream.Collectors.joining;

import io.minispring.container.annotation.Autowired;
import io.minispring.container.annotation.Bean;
import io.minispring.container.annotation.Component;
import io.minispring.container.annotation.Scope;
import io.minispring.container.annotation.ScopeType;
import io.minispring.container.exception.BeanDefinitionException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Turns a class into a {@link BeanDefinition}. It applies the naming, constructor, scope and
 * lifecycle rules, and rejects invalid classes before any bean is created.
 */
public final class BeanDefinitionReader {

    public BeanDefinition read(Class<?> type) {
        return new BeanDefinition(
                nameOf(type),
                type,
                new BeanSource.OfConstructor(constructorOf(type)),
                scopeOf(type),
                LifecycleMethods.of(type));
    }

    /**
     * Reads the {@link Bean} methods of a configuration class. The class itself is read by
     * {@link #read}, because the container needs it to call those methods.
     *
     * <p>Lifecycle callbacks are read from the declared return type. When the method returns an
     * interface, the factory reads them again from the instance's real class.
     */
    public List<BeanDefinition> readBeanMethods(Class<?> configurationClass) {
        String owner = nameOf(configurationClass);
        return Arrays.stream(configurationClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Bean.class))
                // Declared method order is unspecified, so sort to keep startup deterministic.
                .sorted(Comparator.comparing(Method::getName))
                .map(method -> readBeanMethod(owner, method))
                .toList();
    }

    private static BeanDefinition readBeanMethod(String owner, Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            throw new BeanDefinitionException("@Bean method %s.%s must not be static: the container calls it on the configuration bean."
                    .formatted(method.getDeclaringClass().getName(), method.getName()));
        }
        Class<?> type = method.getReturnType();
        if (type == void.class) {
            throw new BeanDefinitionException("@Bean method %s.%s must return the bean it creates."
                    .formatted(method.getDeclaringClass().getName(), method.getName()));
        }
        Bean bean = method.getAnnotation(Bean.class);
        String name = bean.value().isBlank() ? method.getName() : bean.value();
        return new BeanDefinition(
                name,
                type,
                new BeanSource.OfFactoryMethod(owner, method),
                scopeOf(method),
                LifecycleMethods.of(type));
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

    private static ScopeType scopeOf(AnnotatedElement element) {
        Scope scope = element.getAnnotation(Scope.class);
        return scope == null ? ScopeType.SINGLETON : scope.value();
    }

    private static String signature(Constructor<?> constructor) {
        return constructor.getDeclaringClass().getSimpleName() + Arrays.stream(constructor.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(joining(", ", "(", ")"));
    }
}
