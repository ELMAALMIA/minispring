package io.minispring.autoconfigure.fixtures;

import io.minispring.autoconfigure.AutoConfigureAfter;
import io.minispring.autoconfigure.AutoConfiguration;
import io.minispring.autoconfigure.condition.ConditionalOnBean;
import io.minispring.container.annotation.Bean;

/** Registered after the clock, so that its @ConditionalOnBean can see it. */
@AutoConfiguration
@AutoConfigureAfter(ClockAutoConfiguration.class)
public class ReportAutoConfiguration {

    @Bean
    @ConditionalOnBean(Clock.class)
    public String clockReport(Clock clock) {
        return "report of " + clock.now();
    }
}
