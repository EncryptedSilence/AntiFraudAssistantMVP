package com.qalqan.antifraud.settings

import io.kotest.matchers.shouldBe
import org.junit.Test

class RetentionDisplayTest {
    @Test
    fun `lists all §15_2 retention values for the Privacy screen`() {
        val rows = RetentionDisplay.rows()
        rows.size shouldBe 4
        rows[0] shouldBe RetentionDisplay.Row("events_active_horizon_days", 14)
        rows[1] shouldBe RetentionDisplay.Row("events_archive_days", 30)
        rows[2] shouldBe RetentionDisplay.Row("action_log_days", 30)
        rows[3] shouldBe RetentionDisplay.Row("export_profile_days", 30)
    }
}
