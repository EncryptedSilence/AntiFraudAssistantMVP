package com.qalqan.antifraud.ui.home

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
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.R
import com.qalqan.antifraud.ui.state.LoadingState
import com.qalqan.antifraud.ui.state.accessibleTouchTarget

@Composable
fun HomeRoute(
    state: HomeUiState,
    onSuspiciousCall: () -> Unit,
    onSuspiciousSms: () -> Unit,
    onSuspiciousSite: () -> Unit,
    onOpenCampaign: (String) -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    if (state.isLoading) {
        LoadingState()
        return
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HomeStatusHeader(state, onOpenPrivacy)
        ActiveCampaignCardOrEmpty(state, onOpenCampaign)
        QuickActionRow(onSuspiciousCall, onSuspiciousSms, onSuspiciousSite)
    }
}

@Composable
private fun HomeStatusHeader(
    state: HomeUiState,
    @Suppress("UNUSED_PARAMETER") onOpenPrivacy: () -> Unit,
) {
    val bandLabel =
        state.currentBand?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
            ?: stringResource(R.string.home_risk_all_clear)
    Text(stringResource(R.string.home_risk_label, bandLabel), style = MaterialTheme.typography.titleSmall)
    Text(
        stringResource(
            R.string.home_24h_summary,
            state.eventsLast24h,
            state.alertsLast24h,
            state.dismissedLast24h,
        ),
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun ActiveCampaignCardOrEmpty(
    state: HomeUiState,
    onOpenCampaign: (String) -> Unit,
) {
    val card = state.activeCampaign
    if (card == null) {
        Text(stringResource(R.string.home_empty_watching), style = MaterialTheme.typography.bodyLarge)
        return
    }
    Text(stringResource(R.string.home_active_campaign_card_title), style = MaterialTheme.typography.titleMedium)
    Text(stringResource(R.string.home_active_campaign_card_started, card.startedAt.toString()))
    Text(stringResource(R.string.home_active_campaign_card_last_event, card.lastEventAt.toString()))
    Button(onClick = { onOpenCampaign(card.campaignId) }, modifier = Modifier.accessibleTouchTarget()) {
        Text(stringResource(R.string.home_active_campaign_card_open))
    }
}

@Composable
private fun QuickActionRow(
    onSuspiciousCall: () -> Unit,
    onSuspiciousSms: () -> Unit,
    onSuspiciousSite: () -> Unit,
) {
    Button(onClick = onSuspiciousCall, modifier = Modifier.accessibleTouchTarget()) {
        Text(stringResource(R.string.home_suspicious_call_button))
    }
    Button(onClick = onSuspiciousSms, modifier = Modifier.accessibleTouchTarget()) {
        Text(stringResource(R.string.home_suspicious_sms_button))
    }
    Button(onClick = onSuspiciousSite, modifier = Modifier.accessibleTouchTarget()) {
        Text(stringResource(R.string.home_suspicious_site_button))
    }
}
