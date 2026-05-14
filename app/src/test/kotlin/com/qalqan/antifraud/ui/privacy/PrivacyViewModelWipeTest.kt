package com.qalqan.antifraud.ui.privacy

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import com.qalqan.antifraud.settings.UserSettings
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class PrivacyViewModelWipeTest {
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

    private fun drainMain() {
        repeat(200) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(5)
        }
    }

    @Test
    fun `deleteAll clears the database and logs DATA_DELETED`() {
        runBlocking { repos.actionLogger.log(AppAction.APP_START, emptyMap()) }
        viewModel.deleteAll()
        drainMain()
        val recent = runBlocking { repos.actionLog.recent(100) }
        recent.any { it.action == AppAction.DATA_DELETED } shouldBe true
        val callCount = runBlocking { repos.calls.listSince(java.time.Instant.EPOCH).size }
        callCount shouldBe 0
    }

    @Test
    fun `resetPermissions clears the onboarding-completed flag`() {
        val userSettings = UserSettings(context)
        userSettings.onboardingCompleted = true
        viewModel.resetPermissions()
        drainMain()
        UserSettings(context).onboardingCompleted shouldBe false
    }
}
