package com.qalqan.antifraud

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.settings.UserSettings
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AntifraudAppFirstLaunchRoutingTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var repos: Repositories

    @Before
    fun setUp() {
        repos = Repositories.inMemory(context)
        // Suppress the educational card pager so HomeViewModel doesn't keep its refresh
        // coroutine alive past tearDown.
        UserSettings(context).lastEducationalCardAtMs = System.currentTimeMillis()
    }

    @After
    fun tearDown() {
        composeRule.waitForIdle()
        repos.close()
        context.getSharedPreferences("antifraud_user_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun `first launch with no completed onboarding shows the onboarding route first`() {
        UserSettings(context).onboardingCompleted = false
        composeRule.setContent { AntifraudApp(repos = repos) }
        composeRule.onNodeWithText("Welcome").assertIsDisplayed()
    }

    @Test
    fun `second launch with onboarding complete jumps straight to home`() {
        UserSettings(context).onboardingCompleted = true
        composeRule.setContent { AntifraudApp(repos = repos) }
        composeRule.onNodeWithText("Home").assertIsDisplayed()
    }
}
