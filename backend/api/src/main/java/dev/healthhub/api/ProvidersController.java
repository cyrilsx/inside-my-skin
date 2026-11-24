package dev.healthhub.api;

import dev.healthhub.ingestion.ConnectionService;
import dev.healthhub.ingestion.CredentialsStatus;
import dev.healthhub.api.dto.Dtos.ProviderListDto;
import dev.healthhub.ingestion.ProviderInfo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/providers")
public class ProvidersController {

    /** Request body for PUT /providers/{provider}/credentials. Secret values are never returned. */
    public record CredentialsUpdate(
            @NotEmpty @Size(max = 16) Map<@Pattern(regexp = "[A-Za-z0-9_.-]{1,64}") String, @NotEmpty String> credentials) {
    }

    private final ConnectionService connectionService;

    public ProvidersController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @GetMapping
    public ProviderListDto list() {
        return new ProviderListDto(connectionService.listProviders());
    }

    @GetMapping("/{provider}/credentials")
    public CredentialsStatus status(@PathVariable String provider) {
        try {
            return connectionService.credentialsStatus(provider);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
        }
    }

    @PutMapping("/{provider}/credentials")
    public ResponseEntity<Void> save(@PathVariable String provider, @RequestBody CredentialsUpdate body) {
        try {
            connectionService.saveCredentials(provider, body.credentials());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
        }
    }

    @DeleteMapping("/{provider}/credentials")
    public ResponseEntity<Void> delete(@PathVariable String provider) {
        try {
            connectionService.deleteCredentials(provider);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
        }
    }
}
