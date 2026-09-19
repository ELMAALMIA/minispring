package io.minispring.container.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * The code behind every transactional proxy: each call is either passed straight to the bean,
 * or wrapped between {@code begin} and {@code commit}/{@code rollback}.
 */
final class TransactionalInvocationHandler implements InvocationHandler {

    private final Object target;
    /** Every interface method of the target, made accessible once, keyed by the method the proxy receives. */
    private final Map<Method, Method> targetMethods;
    private final Set<Method> transactionalMethods;
    private final TransactionManager transactionManager;

    TransactionalInvocationHandler(Object target, Map<Method, Method> targetMethods, Set<Method> transactionalMethods,
                                   TransactionManager transactionManager) {
        this.target = target;
        this.targetMethods = Map.copyOf(targetMethods);
        this.transactionalMethods = Set.copyOf(transactionalMethods);
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!transactionalMethods.contains(method)) {
            return invokeTarget(method, args);
        }
        String name = target.getClass().getSimpleName() + "." + method.getName();
        transactionManager.begin(name);
        Object result;
        try {
            result = invokeTarget(method, args);
        } catch (RuntimeException | Error e) {
            transactionManager.rollback(name);
            throw e;
        } catch (Throwable checked) {
            // Spring's rule: a checked exception is an expected business outcome, so the work is kept.
            transactionManager.commit(name);
            throw checked;
        }
        transactionManager.commit(name);
        return result;
    }

    private Object invokeTarget(Method method, Object[] args) throws Throwable {
        try {
            return targetMethods.getOrDefault(method, method).invoke(target, args);
        } catch (InvocationTargetException e) {
            // Reflection wraps whatever the bean throws: rethrow the original exception, never the wrapper.
            throw e.getCause();
        }
    }
}
