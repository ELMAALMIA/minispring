package io.minispring.container.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.minispring.container.context.AnnotationApplicationContext;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DispatcherServerTest {

    static class Greeting {
        String text() {
            return "hello";
        }
    }

    @RestController
    static class GreetingController {
        private final Greeting greeting;

        GreetingController(Greeting greeting) {
            this.greeting = greeting;
        }

        @GetMapping("/hello")
        public String hello() {
            return greeting.text();
        }

        @GetMapping("/fail")
        public String fail() {
            throw new IllegalStateException("handler failure");
        }
    }

    private final HttpClient client = HttpClient.newHttpClient();
    private AnnotationApplicationContext context;
    private DispatcherServer server;

    @BeforeEach
    void startServer() {
        context = AnnotationApplicationContext.of(GreetingController.class, Greeting.class);
        server = DispatcherServer.start(context, 0);
    }

    @AfterEach
    void stopServer() {
        server.close();
        context.close();
        client.close();
    }

    @Test
    void routesAGetRequestToTheMappedMethod() throws Exception {
        HttpResponse<String> response = get("/hello");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("hello");
    }

    @Test
    void answers404ForAPathWithoutHandler() throws Exception {
        HttpResponse<String> response = get("/missing");

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).isEqualTo("No handler for /missing");
    }

    @Test
    void answers500WhenTheHandlerThrows() throws Exception {
        assertThat(get("/fail").statusCode()).isEqualTo(500);
    }

    @Test
    void answers405ForAnythingButGet() throws Exception {
        HttpRequest post = HttpRequest.newBuilder(uri("/hello")).POST(HttpRequest.BodyPublishers.noBody()).build();

        assertThat(client.send(post, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(405);
    }

    @Test
    void scanningFindsControllersThroughTheComponentMetaAnnotation() {
        try (var scanned = AnnotationApplicationContext.scan("io.minispring.fixtures.web")) {
            assertThat(scanned.getBeansWithAnnotation(RestController.class)).containsOnlyKeys("statusController");
        }
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + server.port() + path);
    }
}
