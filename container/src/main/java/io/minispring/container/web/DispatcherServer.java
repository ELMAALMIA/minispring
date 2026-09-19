package io.minispring.container.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.minispring.container.context.ApplicationContext;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A deliberately crude web layer: a map from path to controller method, served by the JDK's
 * built-in HTTP server. It shows that MVC dispatching is a lookup table, not magic.
 *
 * <p>Only exact paths, GET requests, no-argument handlers and plain-text responses are
 * supported. The server listens on the loopback interface only.
 */
public final class DispatcherServer implements AutoCloseable {

    private static final System.Logger LOGGER = System.getLogger(DispatcherServer.class.getName());

    private final HttpServer server;
    private final ExecutorService executor;

    /** A controller method that answers one path. */
    private record Route(Object controller, Method handler) {

        Object invoke() throws InvocationTargetException, IllegalAccessException {
            return handler.invoke(controller);
        }
    }

    private DispatcherServer(HttpServer server, ExecutorService executor) {
        this.server = server;
        this.executor = executor;
    }

    /** Starts serving the context's controllers on {@code port}; port 0 picks a free port. */
    public static DispatcherServer start(ApplicationContext context, int port) {
        Map<String, Route> routes = routesOf(context);
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
            server.createContext("/", exchange -> dispatch(exchange, routes));
            server.setExecutor(executor);
            server.start();
            return new DispatcherServer(server, executor);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not start the HTTP server on port " + port, e);
        }
    }

    public int port() {
        return server.getAddress().getPort();
    }

    @Override
    public void close() {
        server.stop(0);
        executor.close();
    }

    private static Map<String, Route> routesOf(ApplicationContext context) {
        Map<String, Route> routes = new HashMap<>();
        context.getBeansWithAnnotation(RestController.class).forEach((name, controller) -> {
            if (Proxy.isProxyClass(controller.getClass())) {
                throw new IllegalStateException("Controller '" + name + "' is a proxy, so its mappings are hidden: move @Transactional methods into a service");
            }
            for (Method method : controller.getClass().getMethods()) {
                GetMapping mapping = method.getAnnotation(GetMapping.class);
                if (mapping == null) {
                    continue;
                }
                if (method.getParameterCount() > 0) {
                    throw new IllegalStateException("Handler %s.%s must not take parameters".formatted(name, method.getName()));
                }
                // A public method of a non-public controller class still needs this to be invoked.
                method.trySetAccessible();
                Route previous = routes.putIfAbsent(mapping.value(), new Route(controller, method));
                if (previous != null) {
                    throw new IllegalStateException("Path %s is mapped twice: %s and %s"
                            .formatted(mapping.value(), previous.handler(), method));
                }
            }
        });
        return Map.copyOf(routes);
    }

    private static void dispatch(HttpExchange exchange, Map<String, Route> routes) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            Route route = routes.get(path);
            if (route == null) {
                respond(exchange, 404, "No handler for " + path);
            } else if (!"GET".equals(exchange.getRequestMethod())) {
                respond(exchange, 405, "Only GET is supported");
            } else {
                respond(exchange, 200, String.valueOf(route.invoke()));
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            Throwable cause = e instanceof InvocationTargetException wrapper ? wrapper.getCause() : e;
            LOGGER.log(System.Logger.Level.WARNING, "Handler for " + exchange.getRequestURI() + " failed", cause);
            respond(exchange, 500, "Internal server error");
        } finally {
            exchange.close();
        }
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
