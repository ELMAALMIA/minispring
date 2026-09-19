package io.minispring.container.transaction;

import static java.util.stream.Collectors.joining;

import io.minispring.container.annotation.Transactional;
import io.minispring.container.bean.BeanPostProcessor;
import io.minispring.container.exception.BeanCreationException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Replaces every bean that has {@link Transactional} methods with a JDK dynamic proxy that
 * opens a transaction around those methods.
 *
 * <p>This is where {@code @Transactional} stops being magic. The container hands out the proxy
 * instead of the bean, and the proxy calls the {@link TransactionManager} before and after it
 * delegates to the bean. A call the bean makes on itself ({@code this.method()}) goes straight
 * to the bean, not through the proxy, so it gets no transaction. Spring behaves the same way.
 */
public final class TransactionalProcessor implements BeanPostProcessor {

    private final TransactionManager transactionManager;

    public TransactionalProcessor(TransactionManager transactionManager) {
        this.transactionManager = Objects.requireNonNull(transactionManager, "transactionManager");
    }

    // S3011: the proxy must be able to call methods of interfaces that are not public.
    @Override
    @SuppressWarnings("java:S3011")
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> type = bean.getClass();
        List<Method> annotated = Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Transactional.class) && !method.isBridge())
                .toList();
        if (annotated.isEmpty()) {
            return bean;
        }
        List<Class<?>> interfaces = interfacesOf(type);
        if (interfaces.isEmpty()) {
            throw new BeanCreationException(beanName, "it has @Transactional methods but implements no interface. "
                    + "JDK dynamic proxies can only implement interfaces, and class-based (CGLIB) proxies are out of scope");
        }

        Map<Method, Method> targetMethods = new HashMap<>();
        Set<Method> transactionalMethods = new HashSet<>();
        Set<Method> interceptable = new HashSet<>();
        for (Method method : interfaceMethodsOf(interfaces)) {
            Method implementation = implementationOf(type, method);
            interceptable.add(implementation);
            method.trySetAccessible();
            targetMethods.put(method, method);
            if (implementation.isAnnotationPresent(Transactional.class)) {
                transactionalMethods.add(method);
            }
        }
        for (Method method : annotated) {
            if (!interceptable.contains(method)) {
                throw new BeanCreationException(beanName, "@Transactional method %s() is not declared by any of its interfaces %s, so the proxy could never intercept it"
                        .formatted(method.getName(), interfaces.stream().map(Class::getSimpleName).collect(joining(", ", "[", "]"))));
            }
        }
        return Proxy.newProxyInstance(
                type.getClassLoader(),
                interfaces.toArray(Class<?>[]::new),
                new TransactionalInvocationHandler(bean, targetMethods, transactionalMethods, transactionManager));
    }

    private static List<Class<?>> interfacesOf(Class<?> type) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            interfaces.addAll(List.of(current.getInterfaces()));
        }
        return List.copyOf(interfaces);
    }

    private static List<Method> interfaceMethodsOf(List<Class<?>> interfaces) {
        return interfaces.stream()
                .flatMap(anInterface -> Arrays.stream(anInterface.getMethods()))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .toList();
    }

    private static Method implementationOf(Class<?> type, Method interfaceMethod) {
        try {
            return type.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("A concrete class always implements the methods of its interfaces", e);
        }
    }
}
