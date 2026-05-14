package com.qalqan.antifraud.settings

import io.kotest.matchers.shouldBe
import org.junit.Test

class EducationalCardSchedulerTest {
    @Test
    fun `shouldShow returns true when never shown before and enabled`() {
        EducationalCardScheduler.shouldShow(
            enabled = true,
            lastShownAtMs = 0L,
            nowMs = 1_700_000_000_000L,
        ) shouldBe true
    }

    @Test
    fun `shouldShow returns false when disabled in settings`() {
        EducationalCardScheduler.shouldShow(
            enabled = false,
            lastShownAtMs = 0L,
            nowMs = 1_700_000_000_000L,
        ) shouldBe false
    }

    @Test
    fun `shouldShow returns false within 24h of last shown`() {
        val now = 1_700_000_000_000L
        val twentyThreeHoursAgo = now - (23L * 60L * 60L * 1000L)
        EducationalCardScheduler.shouldShow(
            enabled = true,
            lastShownAtMs = twentyThreeHoursAgo,
            nowMs = now,
        ) shouldBe false
    }

    @Test
    fun `shouldShow returns true at exactly 24h since last shown`() {
        val now = 1_700_000_000_000L
        val twentyFourHoursAgo = now - (24L * 60L * 60L * 1000L)
        EducationalCardScheduler.shouldShow(
            enabled = true,
            lastShownAtMs = twentyFourHoursAgo,
            nowMs = now,
        ) shouldBe true
    }
}
