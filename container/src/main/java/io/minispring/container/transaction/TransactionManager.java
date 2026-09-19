package io.minispring.container.transaction;

/**
 * Strategy that decides what a transaction actually is. The proxy only decides <em>when</em>
 * to call it.
 *
 * <p>Declare a bean implementing this interface to replace the default
 * {@link ConsoleTransactionManager}.
 */
public interface TransactionManager {

    void begin(String name);

    void commit(String name);

    void rollback(String name);
}
