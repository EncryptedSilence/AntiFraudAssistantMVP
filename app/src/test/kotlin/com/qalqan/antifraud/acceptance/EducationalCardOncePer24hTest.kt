package com.qalqan.antifraud.acceptance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.settings.EducationalCardScheduler
import com.qalqan.antifraud.settings.UserSettings
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §19A — at most one educational card per 24 h.
 */
@RunWith(RobolectricTestRunner::class)
class EducationalCardOncePer24hTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        context.getSharedPreferences("antifraud_user_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun `§19A — second show within 24h is suppressed`() {
        val settings = UserSettings(context)
        val now = 1_700_000_000_000L
        settings.lastEducationalCardAtMs = now
        EducationalCardScheduler.shouldShow(
            enabled = settings.educationalCardsEnabled,
            lastShownAtMs = settings.lastEducationalCardAtMs,
            nowMs = now + (60L * 60L * 1000L),
        ) shouldBe false
    }

    @Test
    fun `§19A — show after 24h gap`() {
        val settings = UserSettings(context)
        val now = 1_700_000_000_000L
        settings.lastEducationalCardAtMs = now
        EducationalCardScheduler.shouldShow(
            enabled = settings.educationalCardsEnabled,
            lastShownAtMs = settings.lastEducationalCardAtMs,
            nowMs = now + (25L * 60L * 60L * 1000L),
        ) shouldBe true
    }
}
