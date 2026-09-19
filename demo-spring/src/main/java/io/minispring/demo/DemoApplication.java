package io.minispring.demo;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Runs the same order scenario on Spring Boot. Pass {@code --serve} to also expose {@code GET /orders}. */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) throws IOException {
        long start = System.nanoTime();
        boolean serve = List.of(args).contains("--serve");
        SpringApplication application = new SpringApplication(DemoApplication.class);
        application.setWebApplicationType(serve ? WebApplicationType.SERVLET : WebApplicationType.NONE);
        try (var context = application.run(args)) {
            // Timing goes to stderr so that stdout stays identical to the minispring demo.
            System.err.printf("Spring Boot context started in %d ms%n", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
            OrderScenario.run(context.getBean(OrderService.class), context.getBean(OrderController.class));
            if (serve) {
                System.err.println("Serving http://localhost:8080/orders, press Enter to stop");
                awaitEnter();
            }
        }
    }

    private static void awaitEnter() throws IOException {
        int character;
        do {
            character = System.in.read();
        } while (character != '\n' && character != -1);
    }
}
