package io.minispring.demo;

import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * The one class the Spring twin needs on top of the shared ones. Spring has no console
 * transaction manager, so this one prints the same lines as minispring's
 * {@code ConsoleTransactionManager}.
 */
@Component
public class ConsoleTransactionManager extends AbstractPlatformTransactionManager {

    /** Carries the transaction's name from begin to commit or rollback. */
    private static final class Transaction {
        private String name;
    }

    @Override
    protected Object doGetTransaction() {
        return new Transaction();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        // Spring names a transaction "package.Class.method"; minispring prints "Class.method".
        String qualifiedName = definition.getName();
        String name = qualifiedName.substring(qualifiedName.lastIndexOf('.', qualifiedName.lastIndexOf('.') - 1) + 1);
        ((Transaction) transaction).name = name;
        print("BEGIN", name);
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        print("COMMIT", ((Transaction) status.getTransaction()).name);
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        print("ROLLBACK", ((Transaction) status.getTransaction()).name);
    }

    private static void print(String event, String name) {
        System.out.printf("%-8s %s%n", event, name);
    }
}
