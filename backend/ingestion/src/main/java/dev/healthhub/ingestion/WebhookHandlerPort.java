package dev.healthhub.ingestion;

import tools.jackson.databind.JsonNode;

public interface WebhookHandlerPort {

    String provider();

    void handle(JsonNode body);
}
