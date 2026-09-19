package io.minispring.container.exception;

/** Thrown when the container cannot build the description of a bean, before anything is instantiated. */
public final class BeanDefinitionException extends ContainerException {

    public BeanDefinitionException(String message) {
        super(message);
    }

    public BeanDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
