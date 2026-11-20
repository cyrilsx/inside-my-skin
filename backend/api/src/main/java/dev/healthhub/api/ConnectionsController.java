package dev.healthhub.api;

import dev.healthhub.ingestion.AuthorizationInit;
import dev.healthhub.ingestion.ConnectionInfo;
import dev.healthhub.ingestion.ConnectionService;
import dev.healthhub.ingestion.SyncResult;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/connections")
public class ConnectionsController {

    private final ConnectionService connectionService;

    public ConnectionsController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @GetMapping
    public List<ConnectionInfo> list() {
        return connectionService.list();
    }

    @GetMapping("/{provider}/authorize")
    public AuthorizationInit authorize(@PathVariable String provider) {
        try {
            return connectionService.startAuthorization(provider);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
        }
    }

    @GetMapping("/{provider}/callback")
    public ConnectionInfo callback(@PathVariable String provider,
            @RequestParam String code, @RequestParam String state) {
        try {
            return connectionService.handleCallback(provider, code, state);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
        }
    }

    @PostMapping("/{id}/sync")
    public SyncResult sync(@PathVariable UUID id) {
        try {
            return connectionService.sync(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable UUID id) {
        try {
            connectionService.revoke(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
}
