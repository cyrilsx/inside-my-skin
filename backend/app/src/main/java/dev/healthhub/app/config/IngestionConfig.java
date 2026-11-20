package dev.healthhub.app.config;

import dev.healthhub.ingestion.CanonicalEventPublisher;
import dev.healthhub.ingestion.CanonicalWritePort;
import dev.healthhub.ingestion.IngestionService;
import dev.healthhub.ingestion.RawEventStore;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IngestionConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    IngestionService ingestionService(RawEventStore rawEventStore, CanonicalWritePort writePort,
            CanonicalEventPublisher eventPublisher, Clock clock) {
        return new IngestionService(rawEventStore, writePort, eventPublisher, clock);
    }
}
