package io.minispring.container.exception;

/** Thrown when a bean exists under the requested name but is not an instance of the requested type. */
public final class BeanNotOfRequiredTypeException extends ContainerException {

    public BeanNotOfRequiredTypeException(String beanName, Class<?> requiredType, Object bean) {
        super("Bean '%s' is a %s, not a %s.".formatted(beanName, bean.getClass().getName(), requiredType.getName()));
    }
}
