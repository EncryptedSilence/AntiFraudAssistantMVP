package com.qalqan.antifraud.ui.campaign

import com.qalqan.antifraud.domain.CampaignStatus
import com.qalqan.antifraud.domain.RiskBand
import java.time.Instant

/**
 * Spec §17.2 — Campaign list state for the tabbed list route.
 */
data class CampaignsUiState(
    val selectedTab: CampaignStatus = CampaignStatus.ACTIVE,
    val rows: List<CampaignRow> = emptyList(),
    val isLoading: Boolean = false,
) {
    data class CampaignRow(
        val campaignId: String,
        val startedAt: Instant,
        val lastEventAt: Instant,
        val band: RiskBand,
    )
}
