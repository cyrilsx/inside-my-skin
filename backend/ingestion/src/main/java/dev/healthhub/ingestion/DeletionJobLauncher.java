package dev.healthhub.ingestion;

import java.util.UUID;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class DeletionJobLauncher {

    private final DeletionService deletionService;

    public DeletionJobLauncher(DeletionService deletionService) {
        this.deletionService = deletionService;
    }

    @Async
    public void launch(UUID jobId) {
        deletionService.executeDeletion(jobId);
    }
}
