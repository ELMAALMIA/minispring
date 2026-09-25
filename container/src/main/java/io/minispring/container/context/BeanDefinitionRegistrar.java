package io.minispring.container.context;

/**
 * Adds definitions once the application's own beans are registered, and before any bean is
 * created.
 *
 * <p>This is the seam auto-configuration plugs into. Running last is what makes "use my bean if
 * I declared one, otherwise give me a default" possible: by then, the registry already shows
 * everything the application declared.
 */
@FunctionalInterface
public interface BeanDefinitionRegistrar {

    void registerDefinitions(RegistrationContext context);
}
