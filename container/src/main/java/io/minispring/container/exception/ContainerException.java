package io.minispring.container.exception;

/**
 * Root of every error the container reports.
 *
 * <p>The hierarchy is sealed: callers can rely on this list of failure modes being complete.
 * Every message names the beans and types involved, so a failure can be fixed from the
 * message alone.
 */
public sealed class ContainerException extends RuntimeException
        permits AmbiguousBeanException, BeanDefinitionException, NoSuchBeanException {

    protected ContainerException(String message) {
        super(message);
    }

    protected ContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}
