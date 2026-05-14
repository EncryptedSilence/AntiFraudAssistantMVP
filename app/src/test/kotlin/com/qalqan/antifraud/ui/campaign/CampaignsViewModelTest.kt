package com.qalqan.antifraud.ui.campaign

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.CampaignStatus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CampaignsViewModelTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)
    private val viewModel =
        CampaignsViewModel(
            application = context.applicationContext as Application,
            repos = repos,
        )

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `default tab is ACTIVE`() {
        viewModel.state.value.selectedTab shouldBe CampaignStatus.ACTIVE
    }

    @Test
    fun `loading active campaigns from empty repo yields empty rows`() {
        runBlocking { viewModel.refresh() }
        viewModel.state.value.rows shouldBe emptyList()
    }
}
