package io.minispring.container.exception;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.Proxy;
import java.util.Arrays;

/** Thrown when a bean exists under the requested name but is not an instance of the requested type. */
public final class BeanNotOfRequiredTypeException extends ContainerException {

    public BeanNotOfRequiredTypeException(String beanName, Class<?> requiredType, Object bean) {
        super(messageFor(beanName, requiredType, bean));
    }

    private static String messageFor(String beanName, Class<?> requiredType, Object bean) {
        String message = "Bean '%s' is a %s, not a %s.".formatted(beanName, bean.getClass().getName(), requiredType.getName());
        if (!Proxy.isProxyClass(bean.getClass())) {
            return message;
        }
        String interfaces = Arrays.stream(bean.getClass().getInterfaces())
                .map(Class::getSimpleName)
                .collect(joining(", ", "[", "]"));
        return message + " It is a JDK dynamic proxy, which only implements the interfaces " + interfaces
                + ": depend on one of those interfaces instead of the class.";
    }
}
