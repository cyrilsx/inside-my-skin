package dev.healthhub.companionapi;

import dev.healthhub.companionapi.IngestDtos.IngestBatchDto;
import dev.healthhub.companionapi.IngestDtos.IngestResultDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companion")
public class IngestController {

    private final CompanionIngestService ingestService;

    public IngestController(CompanionIngestService ingestService) {
        this.ingestService = ingestService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<IngestResultDto> ingest(@Valid @RequestBody IngestBatchDto batch) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ingestService.ingest(batch));
    }
}
