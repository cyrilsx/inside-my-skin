package dev.healthhub.companion.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MoodValidatorTest {

    @Test
    fun rejectsValenceOutsideRange() {
        val draft = MoodDraft(valence = 1.5, labels = emptyList(), note = null)
        try {
            MoodValidator.validate(draft)
            throw AssertionError("expected rejection")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("valence")
        }
    }

    @Test
    fun trimsAndDropsBlankLabels() {
        val valid = MoodValidator.validate(MoodDraft(0.2, listOf(" calm ", "", "tired"), null))
        assertThat(valid.labels).containsExactly("calm", "tired")
        assertThat(valid.valence).isEqualTo(0.2)
    }
}
