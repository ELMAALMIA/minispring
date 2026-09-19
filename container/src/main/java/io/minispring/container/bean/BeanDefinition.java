package io.minispring.container.bean;

import io.minispring.container.annotation.Primary;
import io.minispring.container.annotation.ScopeType;
import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * The description of a bean: everything needed to create it, but not the instance itself.
 *
 * <p>Keeping the description apart from the instance is what makes scopes and proxies possible:
 * the container can create a bean several times, or replace it with a proxy, from one description.
 *
 * @param name        the unique bean name
 * @param type        the class to instantiate
 * @param constructor the constructor used for injection
 * @param scope       how many instances the container creates
 */
public record BeanDefinition(String name, Class<?> type, Constructor<?> constructor, ScopeType scope) {

    public BeanDefinition {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(constructor, "constructor");
        Objects.requireNonNull(scope, "scope");
    }

    public boolean isPrimary() {
        return type.isAnnotationPresent(Primary.class);
    }

    public boolean isSingleton() {
        return scope == ScopeType.SINGLETON;
    }
}
