package dev.healthhub.api;

import dev.healthhub.ingestion.ConnectionService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final ConnectionService connectionService;
    private final String secret;

    public WebhookController(ConnectionService connectionService,
            @Value("${healthhub.webhook.secret:}") String secret) {
        this.connectionService = connectionService;
        this.secret = secret;
    }

    @PostMapping("/{provider}")
    public ResponseEntity<Void> receive(@PathVariable String provider,
            @RequestHeader("X-Webhook-Secret") String providedSecret,
            @RequestBody JsonNode body) {
        if (secret.isBlank() || !MessageDigest.isEqual(
                secret.getBytes(StandardCharsets.UTF_8), providedSecret.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid webhook secret");
        }
        try {
            connectionService.handleWebhook(provider, body);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        return ResponseEntity.accepted().build();
    }
}
