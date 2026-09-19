package io.minispring.container.annotation;

/** How many instances of a bean the container creates. */
public enum ScopeType {

    /** One shared instance, created at startup and cached for the life of the context. */
    SINGLETON,

    /**
     * A new instance for every lookup and every injection point. The container does not keep
     * a reference to it, so it never calls its {@code @PreDestroy} method.
     */
    PROTOTYPE
}
