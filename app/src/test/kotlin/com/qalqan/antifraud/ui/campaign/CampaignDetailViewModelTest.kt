package com.qalqan.antifraud.ui.campaign

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.settings.UserSettings
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class CampaignDetailViewModelTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)
    private val viewModel =
        CampaignDetailViewModel(
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
    fun `load yields a settled state with the requested campaignId when the campaign is absent`() {
        viewModel.load("nonexistent-id")
        // Drain Robolectric's main looper so the viewModelScope coroutine completes; the
        // Room queries it awaits dispatch onto IO threads, so we poll until the state
        // settles (max 100 iterations of 5 ms = 500 ms wall time).
        var settled: CampaignDetailUiState? = null
        repeat(100) {
            shadowOf(Looper.getMainLooper()).idle()
            val state = viewModel.state.value
            if (!state.isLoading && state.campaignId == "nonexistent-id") {
                settled = state
                return@repeat
            }
            Thread.sleep(5)
        }
        checkNotNull(settled) { "viewmodel state never settled" }
        settled!!.isLoading shouldBe false
        settled!!.campaignId shouldBe "nonexistent-id"
    }
}
