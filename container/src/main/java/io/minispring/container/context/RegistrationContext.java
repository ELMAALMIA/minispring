package io.minispring.container.context;

import io.minispring.container.annotation.Configuration;
import io.minispring.container.bean.BeanDefinitionReader;
import io.minispring.container.bean.BeanRegistry;
import io.minispring.container.condition.ConditionEvaluator;
import io.minispring.container.env.Environment;
import java.util.Arrays;
import java.util.Objects;

/**
 * What a {@link BeanDefinitionRegistrar} works with: the definitions registered so far, the
 * condition evaluator, the properties and the class loader.
 *
 * @param registry    the definitions registered so far, and where new ones go
 * @param reader      turns a class into definitions
 * @param conditions  evaluates {@code @Conditional} annotations
 * @param environment the application's properties
 * @param classLoader the loader a registrar should use to look classes up
 */
public record RegistrationContext(
        BeanRegistry registry,
        BeanDefinitionReader reader,
        ConditionEvaluator conditions,
        Environment environment,
        ClassLoader classLoader) {

    public RegistrationContext {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(reader, "reader");
        Objects.requireNonNull(conditions, "conditions");
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(classLoader, "classLoader");
    }

    /**
     * Registers a class and, if it is a {@link Configuration}, its {@code @Bean} methods, unless
     * their conditions reject them. A rejected class takes its {@code @Bean} methods with it.
     *
     * @return true if the class itself was registered
     */
    public boolean registerBeans(Class<?> componentClass) {
        if (!conditions.evaluate(componentClass).matches()) {
            return false;
        }
        registry.register(reader.read(componentClass));
        if (isConfiguration(componentClass)) {
            reader.readBeanMethods(componentClass).stream()
                    .filter(definition -> conditions.evaluate(definition.annotatedElement()).matches())
                    .forEach(registry::register);
        }
        return true;
    }

    /**
     * True for {@link Configuration} itself, and for annotations that carry it, such as an
     * {@code @AutoConfiguration} defined outside the container.
     */
    private static boolean isConfiguration(Class<?> componentClass) {
        return componentClass.isAnnotationPresent(Configuration.class) || Arrays.stream(componentClass.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().isAnnotationPresent(Configuration.class));
    }
}
