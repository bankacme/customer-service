package com.bank.customer.infrastructure.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * One injected {@link Clock}, fixed to {@code bank.zone}, so every use case reads "now"
 * through this bean instead of {@code Instant.now()} — same pattern proven in bank-spike,
 * needed for deterministic time-based tests later.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${bank.zone}") String zone) {
        return Clock.system(ZoneId.of(zone));
    }
}
