package com.qalqan.antifraud.ui.question

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.settings.QuestionPromptKind
import com.qalqan.antifraud.settings.UserSettings
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class QuestionViewModelTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)
    private val viewModel =
        QuestionViewModel(
            application = context.applicationContext as Application,
            repos = repos,
            userSettings = UserSettings(context),
        )

    @After
    fun tearDown() {
        shadowOf(Looper.getMainLooper()).idle()
        repos.close()
        context.getSharedPreferences("antifraud_user_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    private fun drain() {
        repeat(100) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(5)
        }
    }

    @Test
    fun `loadForCampaign emits Q1 at HIGH when nothing answered yet`() {
        viewModel.loadForCampaign(campaignId = "c1", currentBand = RiskBand.HIGH)
        drain()
        viewModel.state.value.kind shouldBe QuestionPromptKind.CALLER_IDENTITY
    }

    @Test
    fun `loadForCampaign emits no kind at MEDIUM`() {
        viewModel.loadForCampaign(campaignId = "c1", currentBand = RiskBand.MEDIUM)
        drain()
        viewModel.state.value.kind shouldBe null
    }

    @Test
    fun `answer records into UserAnswerRepository`() {
        viewModel.loadForCampaign(campaignId = "c1", currentBand = RiskBand.HIGH)
        drain()
        viewModel.answer(
            QuestionPromptKind.CALLER_IDENTITY,
            value = com.qalqan.antifraud.domain.AnswerCode.YES,
            relatedEventId = "ev-1",
        )
        drain()
        val answers = runBlocking { repos.answers.listSince(java.time.Instant.EPOCH) }
        answers.any { it.questionCode == QuestionPromptKind.CALLER_IDENTITY.code } shouldBe true
    }
}
