package io.minispring.container.context;

import io.minispring.container.exception.NoSuchBeanException;

/**
 * The container's public face: the place where application code looks beans up.
 *
 * <p>Closing the context ends the life of every singleton it manages.
 */
public interface ApplicationContext extends AutoCloseable {

    /**
     * Returns the single bean assignable to {@code type}.
     *
     * @throws NoSuchBeanException if no bean matches
     */
    <T> T getBean(Class<T> type);

    /**
     * Returns the bean registered under {@code name}, checked against {@code type}.
     *
     * @throws NoSuchBeanException if no bean has that name
     */
    <T> T getBean(String name, Class<T> type);

    boolean containsBean(String name);

    /** Closes the context. Unlike {@link AutoCloseable#close()}, it never throws a checked exception. */
    @Override
    void close();
}
