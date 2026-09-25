package io.minispring.demo.autoconfigure;

import io.minispring.autoconfigure.AutoConfigurationRegistrar;
import io.minispring.container.context.AnnotationApplicationContext;
import io.minispring.container.env.StandardEnvironment;
import io.minispring.starter.Notifier;
import java.util.Map;

/**
 * Three runs of the same application, showing what decides whether an auto-configured bean
 * appears: what the application declares, and what the properties say.
 */
public final class AutoConfigureDemoApplication {

    private AutoConfigureDemoApplication() {
    }

    public static void main(String[] args) {
        onlyTheStarter();
        applicationDeclaresItsOwn();
        starterTurnedOff();
    }

    /** Nothing is declared, so the starter's default is used. */
    private static void onlyTheStarter() {
        System.out.println("--- the application declares nothing");
        try (var context = AnnotationApplicationContext.builder()
                .apply(new AutoConfigurationRegistrar())
                .build()) {
            context.getBean(Notifier.class).notify("order placed");
        }
    }

    /** The application declares a Notifier, so @ConditionalOnMissingBean steps aside. */
    private static void applicationDeclaresItsOwn() {
        System.out.println("--- the application declares its own notifier");
        try (var context = AnnotationApplicationContext.builder()
                .register(SlackNotifier.class)
                .apply(new AutoConfigurationRegistrar())
                .build()) {
            context.getBean(Notifier.class).notify("order placed");
        }
    }

    /** A property switches the starter off, and the report says so. */
    private static void starterTurnedOff() {
        System.out.println("--- the starter is turned off by a property");
        AutoConfigurationRegistrar registrar = new AutoConfigurationRegistrar();
        try (var context = AnnotationApplicationContext.builder()
                .environment(new StandardEnvironment(Map.of("minispring.notifier.enabled", "false")))
                .apply(registrar)
                .build()) {
            System.out.println(context.containsBean("notifier") ? "a notifier is available" : "no notifier is available");
        }
        System.out.print(registrar.report().toText());
    }
}
