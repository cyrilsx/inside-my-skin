package dev.healthhub.companion.domain

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Test

class UsageAggregatorTest {

    private val berlin = ZoneId.of("Europe/Berlin")

    @Test
    fun aggregatesAndClipsToDayBounds() {
        val date = LocalDate.of(2026, 3, 28)
        val dayStart = date.atStartOfDay(berlin).toInstant().toEpochMilli()
        val events = listOf(
            UsageEvent("com.app.one", dayStart - 3_600_000, dayStart + 1_800_000),
            UsageEvent("com.app.one", dayStart + 3_600_000, dayStart + 5_400_000),
            UsageEvent("com.app.two", dayStart + 7_200_000, dayStart + 9_000_000)
        )
        val day = UsageAggregator.aggregate(events, date, berlin)

        assertThat(day.totalForegroundS).isEqualTo(1800 + 1800 + 1800)
        assertThat(day.perAppSeconds["com.app.one"]).isEqualTo(3600)
        assertThat(day.perAppSeconds["com.app.two"]).isEqualTo(1800)
        assertThat(day.isEstimate).isTrue()
    }

    @Test
    fun dstSpringForwardDayHas23Hours() {
        val dstDay = LocalDate.of(2026, 3, 29)
        val dayStart = dstDay.atStartOfDay(berlin).toInstant().toEpochMilli()
        val dayEnd = dstDay.plusDays(1).atStartOfDay(berlin).toInstant().toEpochMilli()
        val fullDay = UsageEvent("com.app.one", dayStart, dayEnd)

        val day = UsageAggregator.aggregate(listOf(fullDay), dstDay, berlin)

        assertThat(day.totalForegroundS).isEqualTo(23 * 3600)
    }

    @Test
    fun eventsOutsideDayAreIgnored() {
        val date = LocalDate.of(2026, 3, 28)
        val dayStart = date.atStartOfDay(berlin).toInstant().toEpochMilli()
        val dayEnd = date.plusDays(1).atStartOfDay(berlin).toInstant().toEpochMilli()
        val events = listOf(
            UsageEvent("com.app.one", dayStart - 7_200_000, dayStart - 3_600_000),
            UsageEvent("com.app.two", dayEnd, dayEnd + 3_600_000)
        )
        val day = UsageAggregator.aggregate(events, date, berlin)
        assertThat(day.totalForegroundS).isEqualTo(0)
    }
}
