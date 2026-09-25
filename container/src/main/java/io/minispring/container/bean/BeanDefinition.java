package io.minispring.container.bean;

import io.minispring.container.annotation.PostConstruct;
import io.minispring.container.annotation.PreDestroy;
import io.minispring.container.annotation.Primary;
import io.minispring.container.annotation.ScopeType;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * The description of a bean: everything needed to create it, but not the instance itself.
 *
 * <p>Keeping the description apart from the instance is what makes scopes and proxies possible:
 * the container can create a bean several times, or replace it with a proxy, from one description.
 *
 * @param name          the unique bean name
 * @param type          the type the bean will have
 * @param source        how the instance is obtained
 * @param scope         how many instances the container creates
 * @param postConstruct the {@link PostConstruct} method, if any
 * @param preDestroy    the {@link PreDestroy} method, if any
 */
public record BeanDefinition(
        String name,
        Class<?> type,
        BeanSource source,
        ScopeType scope,
        Optional<Method> postConstruct,
        Optional<Method> preDestroy) {

    public BeanDefinition {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(postConstruct, "postConstruct");
        Objects.requireNonNull(preDestroy, "preDestroy");
    }

    /** {@link Primary} sits on the class of a scanned bean, and on the method of a {@code @Bean} bean. */
    public boolean isPrimary() {
        return switch (source) {
            case BeanSource.OfConstructor ignored -> type.isAnnotationPresent(Primary.class);
            case BeanSource.OfFactoryMethod factoryMethod -> factoryMethod.method().isAnnotationPresent(Primary.class);
        };
    }

    public boolean isSingleton() {
        return scope == ScopeType.SINGLETON;
    }
}
