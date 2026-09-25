package io.minispring.autoconfigure;

import io.minispring.container.annotation.Configuration;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a configuration class that a library offers to applications using it.
 *
 * <p>An auto-configuration is not scanned. It is listed in
 * {@value AutoConfigurationImports#LOCATION}, and registered after the application's own beans,
 * so its {@code @ConditionalOnMissingBean} methods only fill what is still missing.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Configuration
public @interface AutoConfiguration {
}
