package io.minispring.autoconfigure.fixtures;

import io.minispring.autoconfigure.AutoConfiguration;
import io.minispring.autoconfigure.condition.ConditionalOnMissingBean;
import io.minispring.autoconfigure.condition.ConditionalOnProperty;
import io.minispring.container.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = "fixtures.clock.enabled", matchIfMissing = true)
public class ClockAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return () -> "default clock";
    }
}
