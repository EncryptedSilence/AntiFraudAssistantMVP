package com.qalqan.antifraud.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserSettingsTogglesTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val settings = UserSettings(context)

    @After
    fun tearDown() {
        context.getSharedPreferences("antifraud_user_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun `all §18 module toggles default to ON`() {
        settings.callAnalysisEnabled shouldBe true
        settings.smsAnalysisEnabled shouldBe true
        settings.webAnalysisEnabled shouldBe true
        settings.riskCampaignsEnabled shouldBe true
        settings.localPatternsEnabled shouldBe true
        settings.referenceSyncEnabled shouldBe true
        settings.patternSyncEnabled shouldBe true
        settings.notificationsEnabled shouldBe true
        settings.postCallQuestionsEnabled shouldBe true
        settings.postSmsQuestionsEnabled shouldBe true
        settings.postSiteQuestionsEnabled shouldBe true
        settings.automaticArchivingEnabled shouldBe true
    }

    @Test
    fun `advanced-rules toggle defaults to OFF per §17_1_1 v2 removal`() {
        settings.advancedRulesEnabled shouldBe false
    }

    @Test
    fun `educational-cards toggle defaults to ON per §19A`() {
        settings.educationalCardsEnabled shouldBe true
    }

    @Test
    fun `onboarding-completed flag defaults to false`() {
        settings.onboardingCompleted shouldBe false
    }

    @Test
    fun `lastEducationalCardAt defaults to 0`() {
        settings.lastEducationalCardAtMs shouldBe 0L
    }

    @Test
    fun `boolean toggles round-trip through SharedPreferences`() {
        settings.callAnalysisEnabled = false
        settings.advancedRulesEnabled = true
        settings.onboardingCompleted = true
        UserSettings(context).callAnalysisEnabled shouldBe false
        UserSettings(context).advancedRulesEnabled shouldBe true
        UserSettings(context).onboardingCompleted shouldBe true
    }

    @Test
    fun `long round-trips`() {
        settings.lastEducationalCardAtMs = 1_700_000_000_000L
        UserSettings(context).lastEducationalCardAtMs shouldBe 1_700_000_000_000L
    }
}
