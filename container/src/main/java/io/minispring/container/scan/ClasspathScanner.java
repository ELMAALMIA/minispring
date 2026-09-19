package io.minispring.container.scan;

import io.minispring.container.annotation.Component;
import io.minispring.container.exception.BeanDefinitionException;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Finds the classes of a package that the container should manage.
 *
 * <p>Java has no API that lists the classes of a package. The scanner asks the class loader
 * where the package's directory is, then walks that directory.
 *
 * <p><b>Deliberate limitation:</b> it only reads directories, so classes packaged in JAR files
 * are not found. That is enough for an application running from its build output.
 */
public final class ClasspathScanner {

    private static final String CLASS_FILE_SUFFIX = ".class";

    private final ClassLoader classLoader;

    public ClasspathScanner() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ClasspathScanner(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
    }

    /**
     * Returns the concrete {@link Component} classes found in {@code basePackage} and its
     * sub-packages, sorted by name so that the result is deterministic.
     */
    public List<Class<?>> scan(String basePackage) {
        if (basePackage == null || basePackage.isBlank()) {
            throw new IllegalArgumentException("The base package must not be blank: scanning the whole classpath is never intended");
        }
        SortedSet<String> classNames = new TreeSet<>();
        for (Path directory : directoriesOf(basePackage)) {
            classNames.addAll(classNamesUnder(directory, basePackage));
        }
        return classNames.stream()
                .<Class<?>>map(this::load)
                .filter(ClasspathScanner::isInstantiableComponent)
                .toList();
    }

    private List<Path> directoriesOf(String basePackage) {
        try {
            List<Path> directories = new ArrayList<>();
            for (URL resource : Collections.list(classLoader.getResources(basePackage.replace('.', '/')))) {
                // "jar:" URLs are skipped on purpose; see the class comment.
                if ("file".equals(resource.getProtocol())) {
                    // toURI() handles spaces and '+' in paths correctly, unlike URLDecoder.
                    directories.add(Path.of(resource.toURI()));
                }
            }
            return directories;
        } catch (IOException | URISyntaxException e) {
            throw new BeanDefinitionException("Could not locate package '" + basePackage + "' on the classpath", e);
        }
    }

    private static List<String> classNamesUnder(Path directory, String basePackage) {
        try (Stream<Path> files = Files.walk(directory)) {
            return files.map(file -> directory.relativize(file).toString())
                    .filter(file -> file.endsWith(CLASS_FILE_SUFFIX))
                    .map(file -> basePackage + '.' + toBinaryName(file))
                    // package-info and module-info are not classes.
                    .filter(name -> !name.contains("-"))
                    .toList();
        } catch (IOException e) {
            throw new BeanDefinitionException("Could not read directory " + directory, e);
        }
    }

    private static String toBinaryName(String relativeClassFile) {
        return relativeClassFile
                .substring(0, relativeClassFile.length() - CLASS_FILE_SUFFIX.length())
                .replace('\\', '.')
                .replace('/', '.');
    }

    private Class<?> load(String className) {
        try {
            // initialize = false: finding a class must not run its static initializers.
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException | LinkageError e) {
            throw new BeanDefinitionException("Could not load class " + className, e);
        }
    }

    private static boolean isInstantiableComponent(Class<?> type) {
        int modifiers = type.getModifiers();
        return isComponent(type)
                && !type.isInterface()
                && !Modifier.isAbstract(modifiers)
                // A non-static inner class cannot exist without an instance of its enclosing class.
                && (!type.isMemberClass() || Modifier.isStatic(modifiers));
    }

    /**
     * True for {@link Component} itself, and for annotations that carry it, such as
     * {@code @RestController}. One level of meta-annotation is enough here.
     */
    private static boolean isComponent(Class<?> type) {
        return type.isAnnotationPresent(Component.class) || Arrays.stream(type.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().isAnnotationPresent(Component.class));
    }
}
