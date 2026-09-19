package io.minispring.container.exception;

/** Thrown when a lookup or an injection point matches no bean. */
public final class NoSuchBeanException extends ContainerException {

    public NoSuchBeanException(String message) {
        super(message);
    }
}
