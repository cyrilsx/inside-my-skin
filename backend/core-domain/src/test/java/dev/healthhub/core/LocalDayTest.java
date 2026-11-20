package dev.healthhub.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LocalDayTest {

    @Test
    void dstSpringTransitionDayBucketsByCapturedOffset() {
        LocalDate dstDay = LocalDate.of(2026, 3, 29);
        ZoneOffset before = ZoneOffset.ofHours(1);
        ZoneOffset after = ZoneOffset.ofHours(2);

        Instant beforeJump = Instant.parse("2026-03-29T00:30:00Z");
        Instant afterJump = Instant.parse("2026-03-29T01:30:00Z");

        assertEquals(dstDay, LocalDay.of(beforeJump, before));
        assertEquals(dstDay, LocalDay.of(afterJump, after));
    }

    @Test
    void utcMidnightCrossingUsesCapturedOffsetNotUtc() {
        Instant justBeforeUtcMidnight = Instant.parse("2026-03-28T23:30:00Z");
        assertEquals(LocalDate.of(2026, 3, 29), LocalDay.of(justBeforeUtcMidnight, ZoneOffset.ofHours(1)));
        assertEquals(LocalDate.of(2026, 3, 28), LocalDay.of(justBeforeUtcMidnight, ZoneOffset.UTC));
    }
}
