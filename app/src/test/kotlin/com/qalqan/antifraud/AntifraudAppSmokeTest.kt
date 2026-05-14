package com.qalqan.antifraud

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.settings.UserSettings
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Spec §17 — the app shell exposes the five top-level destinations.
 * Spec §23 #2 — first launch reaches the home screen without an account prompt; the
 * placeholder text rendered by Home is unrelated to any "Sign in" / "Register" copy.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class AntifraudAppSmokeTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var repos: Repositories

    @Before
    fun setUp() {
        repos = Repositories.inMemory(context)
        UserSettings(context).onboardingCompleted = true
        // Mark the educational card as shown "now" so the pager isn't rendered on the
        // first paint — keeps the smoke assertions focused on the nav bar.
        UserSettings(context).lastEducationalCardAtMs = System.currentTimeMillis()
    }

    @After
    fun tearDown() {
        composeRule.waitForIdle()
        // Intentionally do NOT close `repos`. HomeViewModel.refresh launches Room queries
        // via LaunchedEffect on the Home destination; those coroutines can outlive
        // waitForIdle and would crash on a freshly closed SQLCipher connection pool.
        // The in-memory DB is freed when the Robolectric sandbox is torn down.
        context.getSharedPreferences("antifraud_user_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun `app shell renders all five §17 top-level labels`() {
        composeRule.setContent { AntifraudApp(repos = repos) }
        composeRule.waitForIdle()
        listOf("Home", "Campaigns", "Patterns", "References", "Privacy").forEach { label ->
            composeRule.onAllNodesWithText(label, useUnmergedTree = true).onFirst().assertIsDisplayed()
        }
    }

    @Test
    fun `first-launch shell has no account-prompt copy per §23 #2`() {
        composeRule.setContent { AntifraudApp(repos = repos) }
        composeRule.waitForIdle()
        listOf("Sign in", "Log in", "Register", "Account").forEach { forbidden ->
            val nodes = composeRule.onAllNodesWithText(forbidden, useUnmergedTree = true).fetchSemanticsNodes()
            check(nodes.isEmpty()) { "Forbidden first-launch copy found: $forbidden" }
        }
    }
}
