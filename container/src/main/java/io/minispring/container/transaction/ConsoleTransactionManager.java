package io.minispring.container.transaction;

import java.io.PrintStream;
import java.util.Objects;

/** The default {@link TransactionManager}. It prints each transaction boundary, which makes the proxy's behavior visible. */
public final class ConsoleTransactionManager implements TransactionManager {

    private final PrintStream out;

    // S106: printing to the console is this class's purpose, not a logging shortcut.
    @SuppressWarnings("java:S106")
    public ConsoleTransactionManager() {
        this(System.out);
    }

    public ConsoleTransactionManager(PrintStream out) {
        this.out = Objects.requireNonNull(out, "out");
    }

    @Override
    public void begin(String name) {
        print("BEGIN", name);
    }

    @Override
    public void commit(String name) {
        print("COMMIT", name);
    }

    @Override
    public void rollback(String name) {
        print("ROLLBACK", name);
    }

    private void print(String event, String name) {
        out.printf("%-8s %s%n", event, name);
    }
}
