package dev.healthhub.api;

import dev.healthhub.api.dto.Dtos.DeletionJobDto;
import dev.healthhub.ingestion.DeletionJobLauncher;
import dev.healthhub.ingestion.DeletionService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DeletionController {

    private final DeletionService deletionService;
    private final DeletionJobLauncher launcher;

    public DeletionController(DeletionService deletionService, DeletionJobLauncher launcher) {
        this.deletionService = deletionService;
        this.launcher = launcher;
    }

    @DeleteMapping("/data")
    public ResponseEntity<DeletionJobDto> deleteAllData() {
        var job = deletionService.requestDeletion();
        launcher.launch(job.id());
        return ResponseEntity.accepted().body(new DeletionJobDto(
                job.id(), job.status(),
                OffsetDateTime.ofInstant(job.createdAt(), ZoneOffset.UTC).toString(),
                job.completedAt() == null ? null
                        : OffsetDateTime.ofInstant(job.completedAt(), ZoneOffset.UTC).toString()));
    }
}
