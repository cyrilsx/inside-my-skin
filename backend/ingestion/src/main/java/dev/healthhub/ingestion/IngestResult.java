package dev.healthhub.ingestion;

public record IngestResult(IngestOutcome outcome, long rawEventId) {
}
