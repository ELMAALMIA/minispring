package io.minispring.container.env;

import io.minispring.container.exception.BeanDefinitionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * The default {@link Environment}. Sources are consulted in order, and the first one holding
 * the key wins:
 *
 * <ol>
 *   <li>properties passed to the constructor, which is how a test or a {@code main} overrides anything</li>
 *   <li>JVM system properties</li>
 *   <li>environment variables</li>
 *   <li>{@code application.properties} from the classpath</li>
 * </ol>
 */
public final class StandardEnvironment implements Environment {

    private static final String PROPERTIES_FILE = "application.properties";

    private final List<PropertySource> sources;

    public StandardEnvironment() {
        this(Map.of());
    }

    public StandardEnvironment(Map<String, String> overrides) {
        this(defaultSources(Objects.requireNonNull(overrides, "overrides")));
    }

    public StandardEnvironment(List<PropertySource> sources) {
        this.sources = List.copyOf(sources);
    }

    @Override
    public Optional<String> getProperty(String name) {
        return sources.stream()
                .map(source -> source.getProperty(name))
                .flatMap(Optional::stream)
                .findFirst();
    }

    /** The sources in the order they are consulted; useful when explaining where a value came from. */
    public List<PropertySource> sources() {
        return sources;
    }

    private static List<PropertySource> defaultSources(Map<String, String> overrides) {
        List<PropertySource> sources = new ArrayList<>();
        sources.add(new PropertySource.OfMap("overrides", overrides));
        sources.add(new PropertySource.OfSystemProperties());
        sources.add(PropertySource.OfEnvironmentVariables.ofSystem());
        sources.add(new PropertySource.OfMap(PROPERTIES_FILE, readPropertiesFile()));
        return sources;
    }

    private static Map<String, String> readPropertiesFile() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream(PROPERTIES_FILE)) {
            if (stream == null) {
                return Map.of();
            }
            Properties properties = new Properties();
            properties.load(stream);
            Map<String, String> values = new HashMap<>();
            properties.stringPropertyNames().forEach(key -> values.put(key, properties.getProperty(key)));
            return values;
        } catch (IOException e) {
            throw new BeanDefinitionException("Could not read " + PROPERTIES_FILE + " from the classpath", e);
        }
    }
}
