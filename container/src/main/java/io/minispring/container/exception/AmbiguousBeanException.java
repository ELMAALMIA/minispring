package io.minispring.container.exception;

/** Thrown when several beans match an injection point and nothing says which one to use. */
public final class AmbiguousBeanException extends ContainerException {

    public AmbiguousBeanException(String message) {
        super(message);
    }
}
