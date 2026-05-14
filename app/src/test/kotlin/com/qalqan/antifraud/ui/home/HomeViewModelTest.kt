package com.qalqan.antifraud.ui.home

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)
    private val viewModel =
        HomeViewModel(application = context.applicationContext as Application, repos = repos)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `initial state has no active campaign and zero counters`() {
        runBlocking {
            viewModel.refresh()
            // refresh launches inside viewModelScope; runBlocking on Main dispatcher in
            // Robolectric drains it synchronously before we read state.
        }
        val state = viewModel.state.value
        state.currentBand shouldBe null
        state.eventsLast24h shouldBe 0
        state.alertsLast24h shouldBe 0
        state.activeCampaign shouldBe null
    }
}
