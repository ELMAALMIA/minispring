package io.minispring.demo.autoconfigure;

import io.minispring.container.annotation.Component;
import io.minispring.starter.Notifier;

/** The application's own notifier. Declaring it is enough to push the auto-configured one aside. */
@Component
public class SlackNotifier implements Notifier {

    @Override
    public void notify(String message) {
        System.out.println("SLACK    " + message);
    }
}
