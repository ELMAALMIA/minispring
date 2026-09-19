package io.minispring.container.context;

import io.minispring.container.annotation.Component;
import io.minispring.container.bean.BeanDefinition;
import io.minispring.container.bean.BeanDefinitionReader;
import io.minispring.container.bean.BeanFactory;
import io.minispring.container.bean.BeanPostProcessor;
import io.minispring.container.bean.BeanRegistry;
import io.minispring.container.bean.Dependency;
import io.minispring.container.bean.DependencyResolver;
import io.minispring.container.exception.NoSuchBeanException;
import io.minispring.container.scan.ClasspathScanner;
import java.util.Arrays;
import java.util.List;

/**
 * The container, assembled from its parts. It registers a definition for each component
 * class, then creates every singleton eagerly, so configuration errors appear at startup
 * rather than at first use.
 *
 * <p>This class is a facade. Each step lives in its own collaborator ({@link ClasspathScanner},
 * {@link BeanDefinitionReader}, {@link BeanRegistry}, {@link DependencyResolver} and
 * {@link BeanFactory}), so each one can be read and tested on its own.
 */
public final class AnnotationApplicationContext implements ApplicationContext {

    private final BeanRegistry registry = new BeanRegistry();
    private final DependencyResolver resolver = new DependencyResolver(registry);
    private final BeanFactory beanFactory = new BeanFactory(resolver);
    private boolean closed;

    private AnnotationApplicationContext(List<Class<?>> componentClasses) {
        BeanDefinitionReader reader = new BeanDefinitionReader();
        componentClasses.stream().map(reader::read).forEach(registry::register);
        try {
            refresh();
        } catch (RuntimeException e) {
            // Beans created before the failure may hold resources: release them before giving up.
            beanFactory.destroySingletons();
            throw e;
        }
    }

    /** Creates a context from every {@link Component} class in the given packages and their sub-packages. */
    public static AnnotationApplicationContext scan(String... basePackages) {
        ClasspathScanner scanner = new ClasspathScanner();
        return new AnnotationApplicationContext(Arrays.stream(basePackages)
                .flatMap(basePackage -> scanner.scan(basePackage).stream())
                .toList());
    }

    /** Creates a context from exactly the given classes, without scanning. */
    public static AnnotationApplicationContext of(Class<?>... componentClasses) {
        return new AnnotationApplicationContext(List.of(componentClasses));
    }

    private void refresh() {
        registerPostProcessors();
        registry.definitions().stream()
                .filter(BeanDefinition::isSingleton)
                .forEach(beanFactory::getBean);
    }

    /** Post-processors are beans too, but they must exist before the beans they process. */
    private void registerPostProcessors() {
        registry.definitionsOfType(BeanPostProcessor.class)
                .forEach(definition -> beanFactory.addPostProcessor(beanFactory.getBean(definition, BeanPostProcessor.class)));
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
    public void close() {
        if (!closed) {
            closed = true;
            beanFactory.destroySingletons();
        }
    }

    private void assertOpen() {
        if (closed) {
            throw new IllegalStateException("The application context is closed");
        }
    }
}
