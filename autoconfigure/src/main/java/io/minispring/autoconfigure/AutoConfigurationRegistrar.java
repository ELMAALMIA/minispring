package io.minispring.autoconfigure;

import io.minispring.container.context.BeanDefinitionRegistrar;
import io.minispring.container.context.RegistrationContext;
import java.util.List;

/**
 * Registers the auto-configurations the classpath offers.
 *
 * <p>Add it with {@code AnnotationApplicationContext.builder().apply(new AutoConfigurationRegistrar())}.
 * It runs after the application's own beans, so every {@code @ConditionalOnMissingBean} inside an
 * auto-configuration sees what the application already declared.
 */
public final class AutoConfigurationRegistrar implements BeanDefinitionRegistrar {

    @Override
    public void registerDefinitions(RegistrationContext context) {
        List<Class<?>> autoConfigurations = AutoConfigurationSorter.sort(AutoConfigurationImports.load(context.classLoader()));
        autoConfigurations.forEach(context::registerBeans);
    }
}
