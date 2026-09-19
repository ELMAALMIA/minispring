package io.minispring.container.bean;

import io.minispring.container.exception.BeanCreationException;
import io.minispring.container.exception.BeanNotOfRequiredTypeException;
import io.minispring.container.exception.CircularDependencyException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
 * constructor needs, recursively, then runs its {@code @PostConstruct} method, and finally
 * passes it through every {@link BeanPostProcessor}.
 *
 * <p>A singleton is created once, then served from a cache. A prototype is created on every
 * request and never cached.
 *
 * <p>Not thread-safe. A context is built and used from a single thread, which keeps the
 * creation algorithm easy to follow.
 */
public final class BeanFactory {

    private static final System.Logger LOGGER = System.getLogger(BeanFactory.class.getName());

    private final DependencyResolver resolver;
    private final List<BeanPostProcessor> postProcessors = new ArrayList<>();
    private final Map<Class<?>, Object> resolvableDependencies = new HashMap<>();
    private final Map<String, Object> singletons = new HashMap<>();
    /** Singletons in the order they finished creation: every bean appears after its dependencies. */
    private final List<ManagedBean> creationOrder = new ArrayList<>();
    /** Beans being created, in the order they were requested. Used to detect and describe cycles. */
    private final SequencedSet<String> inCreation = new LinkedHashSet<>();

    /** A singleton together with the definition that describes how to destroy it. */
    private record ManagedBean(BeanDefinition definition, Object instance) {
    }

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

    /**
     * Makes {@code value} injectable wherever exactly {@code type} is required, without making it
     * a bean. This is how beans receive the context itself or its event publisher.
     */
    public void registerResolvableDependency(Class<?> type, Object value) {
        resolvableDependencies.put(type, Objects.requireNonNull(value, "value"));
    }

    /** Registers a post-processor for every bean created from now on. */
    public void addPostProcessor(BeanPostProcessor postProcessor) {
        postProcessors.add(Objects.requireNonNull(postProcessor, "postProcessor"));
    }

    public Object getBean(BeanDefinition definition) {
        return switch (definition.scope()) {
            case SINGLETON -> singleton(definition);
            case PROTOTYPE -> create(definition);
        };
    }

    /**
     * Runs the {@code @PreDestroy} methods of every singleton, in reverse creation order, so a
     * bean is destroyed while the beans it depends on are still usable.
     */
    public void destroySingletons() {
        for (ManagedBean bean : creationOrder.reversed()) {
            bean.definition().preDestroy().ifPresent(method -> destroy(bean, method));
        }
        creationOrder.clear();
        singletons.clear();
    }

    private Object singleton(BeanDefinition definition) {
        // Not computeIfAbsent: creating a bean re-enters this method for its dependencies,
        // and a HashMap must not be modified while computeIfAbsent is running.
        Object singleton = singletons.get(definition.name());
        if (singleton == null) {
            singleton = create(definition);
            singletons.put(definition.name(), singleton);
        }
        return singleton;
    }

    private Object create(BeanDefinition definition) {
        if (!inCreation.add(definition.name())) {
            throw new CircularDependencyException(cycleBackTo(definition.name()));
        }
        try {
            Object bean = instantiate(definition);
            definition.postConstruct().ifPresent(method -> initialize(definition, bean, method));
            if (definition.isSingleton()) {
                // The raw bean, not its proxy: @PreDestroy must reach the object itself.
                creationOrder.add(new ManagedBean(definition, bean));
            }
            // Post-processing comes last, so a proxy always wraps a fully initialized bean.
            return postProcess(definition, bean);
        } finally {
            inCreation.remove(definition.name());
        }
    }

    private Object instantiate(BeanDefinition definition) {
        Constructor<?> constructor = definition.constructor();
        Object[] arguments = Arrays.stream(constructor.getParameters())
                .map(parameter -> argumentFor(parameter, definition))
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

    private Object argumentFor(Parameter parameter, BeanDefinition definition) {
        Object infrastructure = resolvableDependencies.get(parameter.getType());
        if (infrastructure != null) {
            return infrastructure;
        }
        return getBean(resolver.resolve(Dependency.of(parameter, definition.name())), parameter.getType());
    }

    private static void initialize(BeanDefinition definition, Object bean, Method method) {
        try {
            invoke(method, bean);
        } catch (InvocationTargetException e) {
            throw new BeanCreationException(definition.name(), "its @PostConstruct method threw an exception", e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new BeanCreationException(definition.name(), "its @PostConstruct method could not be invoked", e);
        }
    }

    private Object postProcess(BeanDefinition definition, Object bean) {
        Object result = bean;
        for (BeanPostProcessor postProcessor : postProcessors) {
            result = postProcessor.postProcessAfterInitialization(result, definition.name());
            if (result == null) {
                throw new BeanCreationException(definition.name(), postProcessor.getClass().getName() + " returned null");
            }
        }
        return result;
    }

    private static void destroy(ManagedBean bean, Method method) {
        try {
            invoke(method, bean.instance());
        } catch (ReflectiveOperationException e) {
            // A failing callback must not stop the remaining beans from releasing their resources.
            Throwable cause = e instanceof InvocationTargetException wrapper ? wrapper.getCause() : e;
            LOGGER.log(System.Logger.Level.WARNING, "@PreDestroy method of bean '" + bean.definition().name() + "' failed", cause);
        }
    }

    private static void invoke(Method method, Object bean) throws ReflectiveOperationException {
        method.setAccessible(true);
        method.invoke(bean);
    }

    private List<String> cycleBackTo(String name) {
        List<String> cycle = new ArrayList<>(inCreation.stream().dropWhile(bean -> !bean.equals(name)).toList());
        cycle.add(name);
        return cycle;
    }
}
