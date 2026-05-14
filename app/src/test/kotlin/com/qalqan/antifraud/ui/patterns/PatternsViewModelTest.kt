package com.qalqan.antifraud.ui.patterns

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class PatternsViewModelTest {
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

    private fun await() {
        repeat(100) {
            shadowOf(Looper.getMainLooper()).idle()
            if (!viewModel.state.value.isLoading && viewModel.state.value.rows.isNotEmpty()) return
            Thread.sleep(5)
        }
    }

    @Test
    fun `refresh loads the five seed patterns`() {
        viewModel.refresh()
        await()
        viewModel.state.value.rows.size shouldBe 5
    }

    @Test
    fun `setEnabled writes through PatternStateRepository and round-trips`() {
        viewModel.refresh()
        await()
        val first = viewModel.state.value.rows.first()
        viewModel.setEnabled(first.patternId, !first.enabled)
        // Drain main looper so the write coroutine completes before the next refresh.
        repeat(50) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(5)
        }
        viewModel.refresh()
        await()
        viewModel.state.value.rows
            .first { it.patternId == first.patternId }
            .enabled shouldBe !first.enabled
    }

    @Test
    fun `resetDefaults clears overrides`() {
        viewModel.refresh()
        await()
        val first = viewModel.state.value.rows.first()
        viewModel.setEnabled(first.patternId, !first.enabled)
        repeat(50) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(5)
        }
        viewModel.resetDefaults()
        repeat(50) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(5)
        }
        viewModel.refresh()
        await()
        viewModel.state.value.rows
            .first { it.patternId == first.patternId }
            .enabled shouldBe first.enabled
        viewModel.state.value.rows.size shouldBeGreaterThanOrEqual 1
    }
}
