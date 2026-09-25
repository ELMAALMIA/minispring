package io.minispring.autoconfigure;

import io.minispring.container.exception.BeanDefinitionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Reads the auto-configurations a classpath offers, one class name per line, comments starting
 * with {@code #}.
 *
 * <p>Every jar can carry its own copy of the file, which is why a starter works by being on the
 * classpath and nothing else. Resources are readable inside jars, unlike the directories the
 * class scanner walks, and reading a short index is also far cheaper than scanning every class.
 * Spring Boot uses an index file for the same two reasons.
 */
public final class AutoConfigurationImports {

    public static final String LOCATION = "META-INF/minispring/autoconfiguration.imports";

    private AutoConfigurationImports() {
    }

    /** The listed classes, sorted by name so that the starting point never depends on jar order. */
    public static List<Class<?>> load(ClassLoader classLoader) {
        SortedSet<String> classNames = new TreeSet<>();
        try {
            for (URL resource : Collections.list(classLoader.getResources(LOCATION))) {
                classNames.addAll(readLines(resource));
            }
        } catch (IOException e) {
            throw new BeanDefinitionException("Could not read " + LOCATION + " from the classpath", e);
        }
        return classNames.stream().<Class<?>>map(name -> load(name, classLoader)).toList();
    }

    private static List<String> readLines(URL resource) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .toList();
        }
    }

    private static Class<?> load(String className, ClassLoader classLoader) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException | LinkageError e) {
            throw new BeanDefinitionException("%s lists %s, which is not on the classpath".formatted(LOCATION, className), e);
        }
    }
}
