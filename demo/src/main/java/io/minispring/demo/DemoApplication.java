package io.minispring.demo;

import io.minispring.container.context.AnnotationApplicationContext;
import io.minispring.container.web.DispatcherServer;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Runs the order scenario on minispring. Pass {@code --serve} to also expose {@code GET /orders}. */
public final class DemoApplication {

    private static final int PORT = 8080;

    private DemoApplication() {
    }

    public static void main(String[] args) throws IOException {
        long start = System.nanoTime();
        try (var context = AnnotationApplicationContext.scan("io.minispring.demo")) {
            // Timing goes to stderr so that stdout stays identical to the Spring Boot twin.
            System.err.printf("minispring context started in %d ms%n", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
            OrderScenario.run(context.getBean(OrderService.class), context.getBean(OrderController.class));
            if (List.of(args).contains("--serve")) {
                serve(context);
            }
        }
    }

    private static void serve(AnnotationApplicationContext context) throws IOException {
        try (var server = DispatcherServer.start(context, PORT)) {
            System.err.printf("Serving http://localhost:%d/orders, press Enter to stop%n", server.port());
            awaitEnter();
        }
    }

    private static void awaitEnter() throws IOException {
        int character;
        do {
            character = System.in.read();
        } while (character != '\n' && character != -1);
    }
}
