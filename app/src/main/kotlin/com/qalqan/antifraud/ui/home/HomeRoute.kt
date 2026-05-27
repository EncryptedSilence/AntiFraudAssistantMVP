package com.qalqan.antifraud.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.R
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.ui.education.EducationalCardPager
import com.qalqan.antifraud.ui.pause.PauseBeforeActionModal
import com.qalqan.antifraud.ui.state.LoadingState
import com.qalqan.antifraud.ui.theme.riskColor

@Composable
fun HomeRoute(
    state: HomeUiState,
    onOpenCampaign: (String) -> Unit,
    onDismissEducationalCard: () -> Unit = {},
) {
    if (state.isLoading) {
        LoadingState()
        return
    }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RiskLevelCard(
            band = state.currentBand,
            label = stringResource(R.string.home_risk_level_label),
            hint = stringResource(R.string.home_risk_hint),
        )
        ActivityCard(
            title = stringResource(R.string.home_activity_24h),
            calls = state.calls24h,
            sms = state.sms24h,
            sites = state.web24h,
            callsLabel = stringResource(R.string.channel_calls),
            smsLabel = stringResource(R.string.channel_sms),
            sitesLabel = stringResource(R.string.channel_sites),
        )
        ActivityCard(
            title = stringResource(R.string.home_activity_7d),
            calls = state.calls7d,
            sms = state.sms7d,
            sites = state.web7d,
            callsLabel = stringResource(R.string.channel_calls),
            smsLabel = stringResource(R.string.channel_sms),
            sitesLabel = stringResource(R.string.channel_sites),
        )
        ActiveChainsSection(state.activeChains, onOpenCampaign)
        EducationalCardPager(
            visible = state.educationalCardVisible,
            onDismiss = onDismissEducationalCard,
        )
    }
    PauseBeforeActionModal(
        visible = state.currentBand == RiskBand.CRITICAL,
        onPause = { /* Stage 11 records dismissal via ApplicationActionLogger; no-op here. */ },
        onShowDetails = {
            state.activeChains.firstOrNull()?.campaignId?.let { onOpenCampaign(it) }
        },
    )
}

@Composable
private fun ActiveChainsSection(
    chains: List<HomeUiState.ActiveCampaignCard>,
    onOpenCampaign: (String) -> Unit,
) {
    Text(
        stringResource(R.string.home_active_chains),
        style = MaterialTheme.typography.titleSmall,
    )
    if (chains.isEmpty()) {
        Text(
            stringResource(R.string.home_empty_watching),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    chains.forEach { chain ->
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCampaign(chain.campaignId) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(
                        R.string.home_chain_started,
                        chain.startedAt.toString().take(CHAIN_DATE_LEN),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    bandTitle(chain.band),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = riskColor(chain.band),
                )
            }
        }
    }
}

private const val CHAIN_DATE_LEN = 10
