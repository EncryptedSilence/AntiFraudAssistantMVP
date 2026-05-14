package com.qalqan.antifraud.ui.settings

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import com.qalqan.antifraud.scoring.Sensitivity
import com.qalqan.antifraud.settings.UserSettings
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)
    private val userSettings = UserSettings(context)
    private val viewModel =
        SettingsViewModel(
            application = context.applicationContext as Application,
            repos = repos,
            userSettings = userSettings,
        )

    @After
    fun tearDown() {
        shadowOf(Looper.getMainLooper()).idle()
        context.getSharedPreferences("antifraud_user_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()
        repos.close()
    }

    private fun drainMain() {
        repeat(200) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(5)
        }
    }

    @Test
    fun `refresh reads sensitivity and toggles from UserSettings`() {
        userSettings.sensitivity = Sensitivity.HIGH
        userSettings.advancedRulesEnabled = true
        viewModel.refresh()
        drainMain()
        viewModel.state.value.sensitivity shouldBe Sensitivity.HIGH
        viewModel.state.value.toggles[SettingsUiState.SettingKey.ADVANCED_RULES] shouldBe true
    }

    @Test
    fun `setSensitivity writes to UserSettings and logs SETTING_CHANGED`() {
        viewModel.setSensitivity(Sensitivity.MAXIMUM)
        drainMain()
        UserSettings(context).sensitivity shouldBe Sensitivity.MAXIMUM
        val recent = runBlocking { repos.actionLog.recent(100) }
        recent.any { it.action == AppAction.SETTING_CHANGED } shouldBe true
    }

    @Test
    fun `setToggle writes through UserSettings`() {
        viewModel.setToggle(SettingsUiState.SettingKey.CALL_ANALYSIS, false)
        drainMain()
        UserSettings(context).callAnalysisEnabled shouldBe false
    }
}
