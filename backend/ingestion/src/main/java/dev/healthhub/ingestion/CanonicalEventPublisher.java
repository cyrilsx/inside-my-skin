package dev.healthhub.ingestion;

public interface CanonicalEventPublisher {

    void publish(CanonicalEvent event);
}
