package io.minispring.starter;

/** What the starter offers to applications: a way to tell someone that something happened. */
@FunctionalInterface
public interface Notifier {

    void notify(String message);
}
