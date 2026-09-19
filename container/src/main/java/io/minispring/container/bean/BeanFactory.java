package io.minispring.container.bean;

import io.minispring.container.exception.BeanCreationException;
import io.minispring.container.exception.BeanNotOfRequiredTypeException;
import io.minispring.container.exception.CircularDependencyException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedSet;

/**
 * Creates beans from their definitions. Creating a bean first creates everything its
 * constructor needs, recursively.
 *
 * <p>Every bean is a singleton: it is created once, then served from a cache.
 *
 * <p>Not thread-safe. A context is built and used from a single thread, which keeps the
 * creation algorithm easy to follow.
 */
public final class BeanFactory {

    private final DependencyResolver resolver;
    private final Map<String, Object> singletons = new HashMap<>();
    /** Beans being created, in the order they were requested. Used to detect and describe cycles. */
    private final SequencedSet<String> inCreation = new LinkedHashSet<>();

    public BeanFactory(DependencyResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    /** Returns the bean described by {@code definition}, checked against the type the caller needs. */
    public <T> T getBean(BeanDefinition definition, Class<T> requiredType) {
        Object bean = getBean(definition);
        if (!requiredType.isInstance(bean)) {
            throw new BeanNotOfRequiredTypeException(definition.name(), requiredType, bean);
        }
        return requiredType.cast(bean);
    }

    public Object getBean(BeanDefinition definition) {
        // Not computeIfAbsent: creating a bean re-enters this method for its dependencies,
        // and a HashMap must not be modified while computeIfAbsent is running.
        Object singleton = singletons.get(definition.name());
        if (singleton == null) {
            singleton = create(definition);
            singletons.put(definition.name(), singleton);
        }
        return singleton;
    }

    public void destroySingletons() {
        singletons.clear();
    }

    private Object create(BeanDefinition definition) {
        if (!inCreation.add(definition.name())) {
            throw new CircularDependencyException(cycleBackTo(definition.name()));
        }
        try {
            return instantiate(definition);
        } finally {
            inCreation.remove(definition.name());
        }
    }

    private Object instantiate(BeanDefinition definition) {
        Constructor<?> constructor = definition.constructor();
        Object[] arguments = Arrays.stream(constructor.getParameters())
                .map(parameter -> getBean(resolver.resolve(Dependency.of(parameter, definition.name())), parameter.getType()))
                .toArray();
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(arguments);
        } catch (InvocationTargetException e) {
            throw new BeanCreationException(definition.name(), "its constructor threw an exception", e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new BeanCreationException(definition.name(), "its constructor could not be invoked", e);
        }
    }

    private List<String> cycleBackTo(String name) {
        List<String> cycle = new ArrayList<>(inCreation.stream().dropWhile(bean -> !bean.equals(name)).toList());
        cycle.add(name);
        return cycle;
    }
}
