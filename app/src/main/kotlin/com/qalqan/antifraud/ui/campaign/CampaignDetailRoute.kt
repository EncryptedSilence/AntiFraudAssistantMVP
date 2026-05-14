package com.qalqan.antifraud.ui.campaign

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.R
import com.qalqan.antifraud.domain.AnswerCode
import com.qalqan.antifraud.settings.QuestionPromptKind
import com.qalqan.antifraud.ui.question.QuestionPromptCard
import com.qalqan.antifraud.ui.state.LoadingState
import com.qalqan.antifraud.ui.state.accessibleTouchTarget

/**
 * Spec §17.2 — Campaign detail screen. Renders linked events, triggered patterns, ≥3
 * reasons (when supplied), pending questions, recommendations, and an action row.
 */
@Composable
@Suppress("LongParameterList")
fun CampaignDetailRoute(
    state: CampaignDetailUiState,
    onClose: () -> Unit,
    onFalseAlarm: () -> Unit,
    onMarkSuspicious: () -> Unit,
    onExport: () -> Unit,
    onCreatePattern: () -> Unit,
    onAnswerQuestion: (QuestionPromptKind, AnswerCode) -> Unit = { _, _ -> },
    onDontAskQuestion: (QuestionPromptKind) -> Unit = {},
) {
    if (state.isLoading) {
        LoadingState()
        return
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.campaign_detail_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.campaign_card_started, state.startedAt.toString()))
        Text(stringResource(R.string.campaign_card_last_event, state.lastEventAt.toString()))
        Text(stringResource(R.string.campaign_card_risk, state.band.name.lowercase()))
        Section(R.string.campaign_detail_linked_events, state.linkedEvents)
        Section(R.string.campaign_detail_triggered_patterns, state.triggeredPatterns)
        Section(R.string.campaign_detail_reasons, state.reasons, tag = "Reason")
        Section(R.string.campaign_detail_pending_questions, state.pendingQuestions)
        state.pendingPrompt?.let { kind ->
            QuestionPromptCard(
                kind = kind,
                onAnswerYes = { onAnswerQuestion(kind, AnswerCode.YES) },
                onAnswerNo = { onAnswerQuestion(kind, AnswerCode.NO) },
                onAnswerNotSure = { onAnswerQuestion(kind, AnswerCode.NOT_SURE) },
                onDontAskAgain = { onDontAskQuestion(kind) },
            )
        }
        Section(R.string.campaign_detail_recommendations, state.recommendations)
        ActionRow(state, onClose, onFalseAlarm, onMarkSuspicious, onExport, onCreatePattern)
    }
}

@Composable
private fun Section(
    @StringRes titleResId: Int,
    items: List<String>,
    tag: String? = null,
) {
    Text(stringResource(titleResId), style = MaterialTheme.typography.titleMedium)
    if (items.isEmpty()) return
    items.forEach { line ->
        Text(
            line,
            modifier =
                if (tag != null) {
                    Modifier.semantics { contentDescription = tag }
                } else {
                    Modifier
                },
        )
    }
}

@Composable
@Suppress("LongParameterList")
private fun ActionRow(
    state: CampaignDetailUiState,
    onClose: () -> Unit,
    onFalseAlarm: () -> Unit,
    onMarkSuspicious: () -> Unit,
    onExport: () -> Unit,
    onCreatePattern: () -> Unit,
) {
    Button(onClick = onClose, modifier = Modifier.accessibleTouchTarget()) {
        Text(stringResource(R.string.campaign_action_close))
    }
    Button(onClick = onFalseAlarm, modifier = Modifier.accessibleTouchTarget()) {
        Text(stringResource(R.string.campaign_action_false_alarm))
    }
    Button(onClick = onMarkSuspicious, modifier = Modifier.accessibleTouchTarget()) {
        Text(stringResource(R.string.campaign_action_mark_suspicious))
    }
    Button(onClick = onExport, modifier = Modifier.accessibleTouchTarget()) {
        Text(stringResource(R.string.campaign_action_export))
    }
    if (state.advancedRulesEnabled) {
        Button(
            onClick = onCreatePattern,
            modifier =
                Modifier
                    .accessibleTouchTarget()
                    .semantics { contentDescription = "CreatePattern" },
        ) {
            Text(stringResource(R.string.campaign_action_create_pattern))
        }
    }
}
