package dev.healthhub.core;

import java.time.LocalDate;
import java.util.List;

public record UsageDailyRecord(
        LocalDate date,
        long totalScreenTimeS,
        boolean isEstimate,
        List<PerApp> perApp) {

    public record PerApp(String app, long foregroundS) {
        public PerApp {
            if (app == null || app.isBlank() || foregroundS < 0) {
                throw new IllegalArgumentException("app is required and foregroundS must be >= 0");
            }
        }
    }

    public UsageDailyRecord {
        if (date == null || perApp == null) {
            throw new IllegalArgumentException("date and perApp are required");
        }
        if (totalScreenTimeS < 0) {
            throw new IllegalArgumentException("totalScreenTimeS must be >= 0");
        }
        perApp = List.copyOf(perApp);
    }
}
