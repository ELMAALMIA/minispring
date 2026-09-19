package io.minispring.container.exception;

/** Thrown when a bean's definition is valid but creating or initializing the instance fails. */
public final class BeanCreationException extends ContainerException {

    public BeanCreationException(String beanName, String reason, Throwable cause) {
        super("Cannot create bean '%s': %s: %s".formatted(beanName, reason, cause), cause);
    }

    public BeanCreationException(String beanName, String reason) {
        super("Cannot create bean '%s': %s".formatted(beanName, reason));
    }
}
