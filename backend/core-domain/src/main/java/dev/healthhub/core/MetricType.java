package dev.healthhub.core;

public enum MetricType {
    STEPS(CanonicalUnit.COUNT),
    DISTANCE_M(CanonicalUnit.METER),
    SLEEP_SECONDS(CanonicalUnit.SECOND),
    HEART_RATE_BPM(CanonicalUnit.BEATS_PER_MINUTE),
    RESTING_HEART_RATE_BPM(CanonicalUnit.BEATS_PER_MINUTE),
    HRV_RMSSD_S(CanonicalUnit.SECOND),
    STRESS_SCORE(CanonicalUnit.SCORE),
    WEIGHT_KG(CanonicalUnit.KILOGRAM),
    SYSTOLIC_BP_PA(CanonicalUnit.PASCAL),
    DIASTOLIC_BP_PA(CanonicalUnit.PASCAL),
    SCREEN_TIME_S(CanonicalUnit.SECOND);

    private final CanonicalUnit canonicalUnit;

    MetricType(CanonicalUnit canonicalUnit) {
        this.canonicalUnit = canonicalUnit;
    }

    public CanonicalUnit canonicalUnit() {
        return canonicalUnit;
    }
}
