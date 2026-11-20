package dev.healthhub.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MetricTypeTest {

    @Test
    void durationsAreCanonicalSeconds() {
        assertEquals(CanonicalUnit.SECOND, MetricType.SLEEP_SECONDS.canonicalUnit());
        assertEquals(CanonicalUnit.SECOND, MetricType.HRV_RMSSD_S.canonicalUnit());
        assertEquals(CanonicalUnit.SECOND, MetricType.SCREEN_TIME_S.canonicalUnit());
    }

    @Test
    void massAndLengthAreCanonical() {
        assertEquals(CanonicalUnit.KILOGRAM, MetricType.WEIGHT_KG.canonicalUnit());
        assertEquals(CanonicalUnit.METER, MetricType.DISTANCE_M.canonicalUnit());
    }

    @Test
    void countsAndRatesAreDimensionlessOrPerTime() {
        assertEquals(CanonicalUnit.COUNT, MetricType.STEPS.canonicalUnit());
        assertEquals(CanonicalUnit.BEATS_PER_MINUTE, MetricType.HEART_RATE_BPM.canonicalUnit());
        assertEquals(CanonicalUnit.BEATS_PER_MINUTE, MetricType.RESTING_HEART_RATE_BPM.canonicalUnit());
        assertEquals(CanonicalUnit.SCORE, MetricType.STRESS_SCORE.canonicalUnit());
    }
}
