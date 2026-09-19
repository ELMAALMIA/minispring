package io.minispring.container.bean;

import io.minispring.container.annotation.PostConstruct;
import io.minispring.container.annotation.PreDestroy;
import io.minispring.container.annotation.Primary;
import io.minispring.container.annotation.ScopeType;
import java.lang.reflect.Constructor;
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
 * @param type          the class to instantiate
 * @param constructor   the constructor used for injection
 * @param scope         how many instances the container creates
 * @param postConstruct the {@link PostConstruct} method, if any
 * @param preDestroy    the {@link PreDestroy} method, if any
 */
public record BeanDefinition(
        String name,
        Class<?> type,
        Constructor<?> constructor,
        ScopeType scope,
        Optional<Method> postConstruct,
        Optional<Method> preDestroy) {

    public BeanDefinition {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(constructor, "constructor");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(postConstruct, "postConstruct");
        Objects.requireNonNull(preDestroy, "preDestroy");
    }

    public boolean isPrimary() {
        return type.isAnnotationPresent(Primary.class);
    }

    public boolean isSingleton() {
        return scope == ScopeType.SINGLETON;
    }
}
