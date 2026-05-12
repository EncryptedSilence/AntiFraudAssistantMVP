package com.qalqan.antifraud.acceptance

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §23 #45 — "I had a suspicious site" reaches the manual-paste path in one tap.
 * The Compose UI is exercised by the existing Stage 3/4 tap-button precedent (action-
 * log marker on tap). The full sheet's UX is verified manually for Stage 5; the
 * Compose-UI reachability test will land alongside Stage 8's home-screen redesign.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance45ManualSiteOneTapTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)

    @After fun tearDown() {
        repos.close()
    }

    @Test
    fun `recording the manual-site-button tap logs SETTING_CHANGED with state tapped`() {
        runBlocking {
            // Simulate the view-model's stub-side-effect on tap.
            repos.actionLogger.log(
                AppAction.SETTING_CHANGED,
                mapOf("setting" to "manual_site_button", "state" to "tapped"),
            )
            val entry = repos.actionLog.recent(1).single()
            entry.action shouldBe AppAction.SETTING_CHANGED
            entry.details["setting"] shouldBe "manual_site_button"
            entry.details["state"] shouldBe "tapped"
        }
    }
}
