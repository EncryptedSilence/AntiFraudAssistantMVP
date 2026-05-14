package com.qalqan.antifraud.ui.campaign

import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.settings.QuestionPromptKind
import java.time.Instant

/**
 * Spec §17.2 — Campaign detail data shape. All collections may be empty; the route
 * renders section headers only when their list is non-empty (except "Linked events"
 * which always shows the heading).
 */
data class CampaignDetailUiState(
    val campaignId: String = "",
    val startedAt: Instant = Instant.EPOCH,
    val lastEventAt: Instant = Instant.EPOCH,
    val band: RiskBand = RiskBand.LOW,
    val linkedEvents: List<String> = emptyList(),
    val triggeredPatterns: List<String> = emptyList(),
    val reasons: List<String> = emptyList(),
    val pendingQuestions: List<String> = emptyList(),
    val pendingPrompt: QuestionPromptKind? = null,
    val recommendations: List<String> = emptyList(),
    val advancedRulesEnabled: Boolean = false,
    val isLoading: Boolean = false,
)
