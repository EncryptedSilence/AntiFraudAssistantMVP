package com.qalqan.antifraud.ui.question

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AnswerCode
import com.qalqan.antifraud.domain.AnswerId
import com.qalqan.antifraud.domain.CampaignId
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.UserAnswer
import com.qalqan.antifraud.settings.QuestionFatigueGate
import com.qalqan.antifraud.settings.QuestionPromptKind
import com.qalqan.antifraud.settings.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

/**
 * Spec §5.5 + §16.5 — bridges the [QuestionFatigueGate] (pure logic in `:feature:settings`)
 * with the [UserAnswerRepository] (Stage 1 persistence). Loaded with a campaign + current
 * risk band; emits the next [QuestionPromptKind] to surface, or `null` to suppress.
 */
class QuestionViewModel(
    application: Application,
    private val repos: Repositories,
    private val userSettings: UserSettings,
) : AndroidViewModel(application) {
    data class State(
        val campaignId: String = "",
        val kind: QuestionPromptKind? = null,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun loadForCampaign(
        campaignId: String,
        currentBand: RiskBand,
        dontAskAgain: Boolean = false,
    ) {
        viewModelScope.launch {
            val twentyFourHoursAgo = Instant.now().minusSeconds(SECONDS_PER_DAY)
            val priorAnswers =
                repos.answers
                    .listSince(twentyFourHoursAgo)
                    .filter { it.relatedCampaignId?.value == campaignId }
            val answeredKinds =
                priorAnswers
                    .map { QuestionPromptKind.fromCode(it.questionCode) }
                    .toSet()
            val gate = QuestionFatigueGate(allowedKinds = allowedKinds())
            val next =
                gate.nextPrompt(
                    campaignId = campaignId,
                    currentBand = currentBand,
                    answeredKinds = answeredKinds,
                    promptsLast24h = priorAnswers.size,
                    dontAskAgain = dontAskAgain,
                    now = Instant.now(),
                )
            _state.value = State(campaignId = campaignId, kind = next)
        }
    }

    fun answer(
        kind: QuestionPromptKind,
        value: AnswerCode,
        relatedEventId: String,
    ) {
        viewModelScope.launch {
            val answer =
                UserAnswer(
                    id = AnswerId(UUID.randomUUID().toString()),
                    relatedEventId = EventId(relatedEventId),
                    relatedSessionId = null,
                    relatedCampaignId =
                        _state.value.campaignId
                            .takeIf { it.isNotBlank() }
                            ?.let { CampaignId(it) },
                    questionCode = kind.code,
                    answerCode = value,
                    userNoteLocalEnc = null,
                    answerRiskScore = 0,
                    createdAt = Instant.now(),
                )
            repos.answers.save(answer)
        }
    }

    private fun allowedKinds(): Set<QuestionPromptKind> =
        buildSet {
            if (userSettings.postCallQuestionsEnabled) add(QuestionPromptKind.CALLER_IDENTITY)
            if (userSettings.postSmsQuestionsEnabled || userSettings.postSiteQuestionsEnabled) {
                add(QuestionPromptKind.PRESSURE)
                add(QuestionPromptKind.ACTION_REQUEST)
            }
        }

    private companion object {
        const val SECONDS_PER_DAY: Long = 24L * 60L * 60L
    }
}
