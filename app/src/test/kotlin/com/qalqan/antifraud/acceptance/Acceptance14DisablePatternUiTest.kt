package com.qalqan.antifraud.acceptance

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.ui.patterns.PatternsViewModel
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * Spec §23 #14 — a disabled pattern does not contribute to scoring.
 *
 * Stage 2's `DisabledPatternAcceptanceTest` proves the repository boundary. This test
 * proves the §17.3 Patterns screen's toggle reaches the same boundary.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance14DisablePatternUiTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)
    private val viewModel =
        PatternsViewModel(
            application = context.applicationContext as Application,
            repos = repos,
        )

    @After
    fun tearDown() {
        shadowOf(Looper.getMainLooper()).idle()
        repos.close()
    }

    @Test
    fun `§23 #14 — UI toggle disables the pattern in PatternStateRepository`() {
        viewModel.refresh()
        repeat(100) {
            shadowOf(Looper.getMainLooper()).idle()
            if (viewModel.state.value.rows.isNotEmpty()) return@repeat
            Thread.sleep(5)
        }
        val firstEnabled = viewModel.state.value.rows.first { it.enabled }
        viewModel.setEnabled(firstEnabled.patternId, enabled = false)
        repeat(50) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(5)
        }
        val effective =
            runBlocking { repos.patternState.isEnabled(firstEnabled.patternId, default = true) }
        effective shouldBe false
    }
}
