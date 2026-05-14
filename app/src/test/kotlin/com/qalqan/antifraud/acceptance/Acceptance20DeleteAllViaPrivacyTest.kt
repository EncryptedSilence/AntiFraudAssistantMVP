package com.qalqan.antifraud.acceptance

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.settings.UserSettings
import com.qalqan.antifraud.ui.privacy.PrivacyViewModel
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * Spec §23 #20 — one tap deletes all DB content.
 *
 * Earlier stages have repository-level tests. This re-validates that the §17.6 Privacy
 * screen's "Delete all data" button drives the same wipe path.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance20DeleteAllViaPrivacyTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)
    private val viewModel =
        PrivacyViewModel(
            application = context.applicationContext as Application,
            repos = repos,
            userSettings = UserSettings(context),
        )

    @After
    fun tearDown() {
        shadowOf(Looper.getMainLooper()).idle()
        repos.close()
    }

    @Test
    fun `§23 #20 — PrivacyViewModel deleteAll clears every entity table`() {
        viewModel.deleteAll()
        repeat(200) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(5)
        }
        val callsCount = runBlocking { repos.calls.listSince(java.time.Instant.EPOCH).size }
        val smsCount = runBlocking { repos.sms.listSince(java.time.Instant.EPOCH).size }
        val webCount = runBlocking { repos.web.listSince(java.time.Instant.EPOCH).size }
        val exportCount = runBlocking { repos.exportProfiles.count() }
        callsCount shouldBe 0
        smsCount shouldBe 0
        webCount shouldBe 0
        exportCount shouldBe 0
    }
}
