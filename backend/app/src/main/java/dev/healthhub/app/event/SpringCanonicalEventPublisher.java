package dev.healthhub.app.event;

import dev.healthhub.ingestion.CanonicalEvent;
import dev.healthhub.ingestion.CanonicalEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringCanonicalEventPublisher implements CanonicalEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringCanonicalEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(CanonicalEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
