package com.qalqan.antifraud

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.settings.UserSettings
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
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

    companion object {
        private lateinit var sharedRepos: Repositories
        private lateinit var context: Context

        @JvmStatic
        @BeforeClass
        fun setUpClass() {
            context = ApplicationProvider.getApplicationContext()
            sharedRepos = Repositories.inMemory(context)
        }

        @JvmStatic
        @AfterClass
        fun tearDownClass() {
            sharedRepos.close()
        }
    }

    @Before
    fun setUp() {
        // Land on Home so the nav-bar test sees all five labels.
        UserSettings(context).onboardingCompleted = true
    }

    @Test
    fun `app shell renders all five §17 top-level labels`() {
        composeRule.setContent { AntifraudApp(repos = sharedRepos) }
        composeRule.waitForIdle()
        listOf("Home", "Campaigns", "Patterns", "References", "Privacy").forEach { label ->
            composeRule.onAllNodesWithText(label, useUnmergedTree = true).onFirst().assertIsDisplayed()
        }
    }

    @Test
    fun `first-launch shell has no account-prompt copy per §23 #2`() {
        composeRule.setContent { AntifraudApp(repos = sharedRepos) }
        composeRule.waitForIdle()
        listOf("Sign in", "Log in", "Register", "Account").forEach { forbidden ->
            val nodes = composeRule.onAllNodesWithText(forbidden, useUnmergedTree = true).fetchSemanticsNodes()
            check(nodes.isEmpty()) { "Forbidden first-launch copy found: $forbidden" }
        }
    }
}
