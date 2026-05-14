package com.qalqan.antifraud.ui.campaign

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qalqan.antifraud.R
import com.qalqan.antifraud.domain.CampaignStatus
import com.qalqan.antifraud.ui.state.EmptyState

/**
 * Spec §17.2 — tabbed list of campaigns by [CampaignStatus]. Empty state renders the
 * §17.1.3 watching copy.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignListRoute(
    state: CampaignsUiState,
    onOpenCampaign: (String) -> Unit,
) {
    val tabs =
        listOf(
            CampaignStatus.ACTIVE to R.string.campaign_tab_active,
            CampaignStatus.CLOSED to R.string.campaign_tab_closed,
            CampaignStatus.ARCHIVED to R.string.campaign_tab_archived,
            CampaignStatus.FALSE_POSITIVE to R.string.campaign_tab_false_positive,
        )
    var selectedIndex by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedIndex) {
            tabs.forEachIndexed { i, (_, label) ->
                Tab(selected = i == selectedIndex, onClick = { selectedIndex = i }) {
                    Text(stringResource(label), modifier = Modifier.padding(16.dp))
                }
            }
        }
        if (state.rows.isEmpty()) {
            EmptyState(messageResId = R.string.campaign_list_empty)
        } else {
            LazyColumn {
                items(state.rows) { row ->
                    CampaignRowCard(row, onOpenCampaign)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampaignRowCard(
    row: CampaignsUiState.CampaignRow,
    onOpen: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        onClick = { onOpen(row.campaignId) },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.campaign_card_started, row.startedAt.toString()))
            Text(stringResource(R.string.campaign_card_last_event, row.lastEventAt.toString()))
            Text(stringResource(R.string.campaign_card_risk, row.band.name.lowercase()))
        }
    }
}
