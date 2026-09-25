package io.minispring.starter;

import io.minispring.autoconfigure.AutoConfiguration;
import io.minispring.autoconfigure.condition.ConditionalOnMissingBean;
import io.minispring.autoconfigure.condition.ConditionalOnProperty;
import io.minispring.container.annotation.Bean;

/**
 * The starter's whole configuration: give the application a console notifier, unless it declared
 * one itself, and unless it turned the starter off.
 *
 * <p>In a real project this class ships in its own jar, together with the
 * {@code META-INF/minispring/autoconfiguration.imports} file that lists it. Nothing else is
 * needed for it to take effect.
 */
@AutoConfiguration
@ConditionalOnProperty(name = "minispring.notifier.enabled", matchIfMissing = true)
public class NotifierAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Notifier notifier() {
        return message -> System.out.println("NOTIFY   " + message);
    }
}
