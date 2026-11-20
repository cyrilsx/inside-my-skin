package dev.healthhub.ingestion;

public interface SyncAuditStore {

    void record(SyncAuditEntry entry);

    void deleteAll();
}
