package io.minispring.container.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Where a bean comes from: its own constructor, or a factory method on another bean.
 *
 * <p>The hierarchy is sealed, so {@link BeanFactory} covers both cases in a single switch and
 * the compiler rejects a forgotten branch. Both forms expose an {@link Executable}, which is
 * what dependency resolution actually needs: a list of parameters to satisfy.
 */
public sealed interface BeanSource {

    /** The constructor or method whose parameters the container injects. */
    Executable executable();

    /** A bean created by calling a constructor. */
    record OfConstructor(Constructor<?> constructor) implements BeanSource {

        public OfConstructor {
            Objects.requireNonNull(constructor, "constructor");
        }

        @Override
        public Executable executable() {
            return constructor;
        }
    }

    /** A bean returned by a {@code @Bean} method of the configuration bean {@code configurationBeanName}. */
    record OfFactoryMethod(String configurationBeanName, Method method) implements BeanSource {

        public OfFactoryMethod {
            Objects.requireNonNull(configurationBeanName, "configurationBeanName");
            Objects.requireNonNull(method, "method");
        }

        @Override
        public Executable executable() {
            return method;
        }
    }
}
