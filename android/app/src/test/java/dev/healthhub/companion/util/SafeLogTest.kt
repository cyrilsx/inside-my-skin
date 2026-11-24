package dev.healthhub.companion.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeLogTest {

    @Test
    fun `masks json credentials and bearer tokens`() {
        val out = SafeLog.redact("""{"clientSecret":"abc123","provider":"garmin"}""")
        assertFalse(out.contains("abc123"))
        assertTrue(out.contains(""""clientSecret":"***""""))
        assertTrue(out.contains(""""provider":"garmin""""))
        assertFalse(SafeLog.redact("Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.x.y").contains("eyJhbGciOiJIUzI1NiJ9"))
    }

    @Test
    fun `masks query params, keeps harmless text`() {
        val out = SafeLog.redact("/callback?code=xyz789&foo=bar")
        assertFalse(out.contains("xyz789"))
        assertTrue(out.contains("foo=bar"))
        assertEquals("uploaded 5 items", SafeLog.redact("uploaded 5 items"))
    }
}
