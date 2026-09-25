package io.minispring.container.context;

import io.minispring.container.annotation.Component;
import io.minispring.container.annotation.Configuration;
import io.minispring.container.bean.BeanDefinition;
import io.minispring.container.bean.BeanDefinitionReader;
import io.minispring.container.bean.BeanFactory;
import io.minispring.container.bean.BeanPostProcessor;
import io.minispring.container.bean.BeanRegistry;
import io.minispring.container.bean.Dependency;
import io.minispring.container.bean.DependencyResolver;
import io.minispring.container.condition.ConditionContext;
import io.minispring.container.condition.ConditionEvaluator;
import io.minispring.container.event.ApplicationEvent;
import io.minispring.container.event.ApplicationEventMulticaster;
import io.minispring.container.event.ApplicationEventPublisher;
import io.minispring.container.event.ApplicationListener;
import io.minispring.container.env.Environment;
import io.minispring.container.env.StandardEnvironment;
import io.minispring.container.exception.NoSuchBeanException;
import io.minispring.container.scan.ClasspathScanner;
import io.minispring.container.transaction.ConsoleTransactionManager;
import io.minispring.container.transaction.TransactionManager;
import io.minispring.container.transaction.TransactionalProcessor;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The container, assembled from its parts. It registers a definition for each component
 * class, then creates every singleton eagerly, so configuration errors appear at startup
 * rather than at first use.
 *
 * <p>This class is a facade. Each step lives in its own collaborator ({@link ClasspathScanner},
 * {@link BeanDefinitionReader}, {@link BeanRegistry}, {@link DependencyResolver} and
 * {@link BeanFactory}), so each one can be read and tested on its own.
 *
 * <p>Create one with {@link #builder()}, or with the {@link #scan} and {@link #of} shortcuts.
 */
public final class AnnotationApplicationContext implements ApplicationContext {

    private final BeanRegistry registry = new BeanRegistry();
    private final DependencyResolver resolver = new DependencyResolver(registry);
    private final BeanFactory beanFactory = new BeanFactory(registry, resolver);
    private final ApplicationEventMulticaster eventMulticaster = new ApplicationEventMulticaster(this::listeners);
    private final Environment environment;
    private final ConditionEvaluator conditionEvaluator;
    private boolean closed;

    private AnnotationApplicationContext(Collection<Class<?>> componentClasses, Environment environment) {
        this.environment = environment;
        this.conditionEvaluator = new ConditionEvaluator(
                new ConditionContext(registry, environment, Thread.currentThread().getContextClassLoader()));
        BeanDefinitionReader reader = new BeanDefinitionReader();
        for (Class<?> componentClass : componentClasses) {
            registerIfConditionsMatch(reader, componentClass);
        }
    }

    /**
     * A bean whose conditions do not match is simply not registered. A rejected
     * {@code @Configuration} class takes its {@code @Bean} methods with it.
     */
    private void registerIfConditionsMatch(BeanDefinitionReader reader, Class<?> componentClass) {
        if (!conditionEvaluator.evaluate(componentClass).matches()) {
            return;
        }
        registry.register(reader.read(componentClass));
        if (componentClass.isAnnotationPresent(Configuration.class)) {
            reader.readBeanMethods(componentClass).stream()
                    .filter(definition -> conditionEvaluator.evaluate(definition.annotatedElement()).matches())
                    .forEach(registry::register);
        }
    }

    /** Starts describing a context: packages to scan and classes to register. */
    public static Builder builder() {
        return new Builder();
    }

    /** Creates a context from every {@link Component} class in the given packages and their sub-packages. */
    public static AnnotationApplicationContext scan(String... basePackages) {
        return builder().scan(basePackages).build();
    }

    /** Creates a context from exactly the given classes, without scanning. */
    public static AnnotationApplicationContext of(Class<?>... componentClasses) {
        return builder().register(componentClasses).build();
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    private void refresh() {
        beanFactory.registerResolvableDependency(ApplicationContext.class, this);
        beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
        beanFactory.registerResolvableDependency(Environment.class, environment);
        try {
            registerPostProcessors();
            registry.definitions().stream()
                    .filter(BeanDefinition::isSingleton)
                    .forEach(beanFactory::getBean);
            publishEvent(new ContextRefreshedEvent(this));
        } catch (RuntimeException e) {
            // Beans created before the failure may hold resources: release them before giving up.
            beanFactory.destroySingletons();
            throw e;
        }
    }

    /**
     * Post-processors are beans too, but they must exist before the beans they process. The
     * built-in transactional processor comes first, like Spring's
     * {@code @EnableTransactionManagement}.
     */
    private void registerPostProcessors() {
        beanFactory.addPostProcessor(new TransactionalProcessor(transactionManager()));
        registry.definitionsOfType(BeanPostProcessor.class)
                .forEach(definition -> beanFactory.addPostProcessor(beanFactory.getBean(definition, BeanPostProcessor.class)));
    }

    /** The application's own {@link TransactionManager} bean if it declares one, the console one otherwise. */
    private TransactionManager transactionManager() {
        if (registry.definitionsOfType(TransactionManager.class).isEmpty()) {
            return new ConsoleTransactionManager();
        }
        return beanFactory.getBean(resolver.resolve(Dependency.on(TransactionManager.class)), TransactionManager.class);
    }

    @Override
    public <T> T getBean(Class<T> type) {
        assertOpen();
        return beanFactory.getBean(resolver.resolve(Dependency.on(type)), type);
    }

    @Override
    public <T> T getBean(String name, Class<T> type) {
        assertOpen();
        BeanDefinition definition = registry.find(name)
                .orElseThrow(() -> new NoSuchBeanException("No bean named '%s' is registered.".formatted(name)));
        return beanFactory.getBean(definition, type);
    }

    @Override
    public boolean containsBean(String name) {
        return registry.contains(name);
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
        assertOpen();
        Map<String, Object> beans = new LinkedHashMap<>();
        registry.definitions().stream()
                .filter(definition -> definition.type().isAnnotationPresent(annotationType))
                .forEach(definition -> beans.put(definition.name(), beanFactory.getBean(definition)));
        return Collections.unmodifiableMap(beans);
    }

    @Override
    public void publishEvent(ApplicationEvent event) {
        assertOpen();
        eventMulticaster.publishEvent(event);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        try {
            publishEvent(new ContextClosedEvent(this));
        } finally {
            closed = true;
            beanFactory.destroySingletons();
        }
    }

    /** Listener beans, looked up on each publication; see {@link ApplicationEventMulticaster}. */
    private List<ApplicationListener<?>> listeners() {
        return registry.definitionsOfType(ApplicationListener.class).stream()
                .<ApplicationListener<?>>map(definition -> beanFactory.getBean(definition, ApplicationListener.class))
                .toList();
    }

    private void assertOpen() {
        if (closed) {
            throw new IllegalStateException("The application context is closed");
        }
    }

    /**
     * Collects component classes from scanned packages and explicit registrations, then builds
     * and starts the context in one step. A context is therefore never visible half-started.
     */
    public static final class Builder {

        private final ClasspathScanner scanner = new ClasspathScanner();
        /** A set, so that a class both scanned and registered becomes a single bean. */
        private final Set<Class<?>> componentClasses = new LinkedHashSet<>();
        private Environment environment = new StandardEnvironment();

        private Builder() {
        }

        /** Replaces the default environment, for example to force a property in a test. */
        public Builder environment(Environment environment) {
            this.environment = Objects.requireNonNull(environment, "environment");
            return this;
        }

        public Builder scan(String... basePackages) {
            for (String basePackage : basePackages) {
                componentClasses.addAll(scanner.scan(basePackage));
            }
            return this;
        }

        public Builder register(Class<?>... classes) {
            componentClasses.addAll(List.of(classes));
            return this;
        }

        public AnnotationApplicationContext build() {
            AnnotationApplicationContext context = new AnnotationApplicationContext(componentClasses, environment);
            context.refresh();
            return context;
        }
    }
}
