package com.qalqan.antifraud.ui.campaign

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.CampaignStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Spec §17.2 — ViewModel for the Campaign list. Holds the selected tab and the
 * rows for that tab, sourced from [Repositories.campaigns].
 */
class CampaignsViewModel(
    application: Application,
    private val repos: Repositories,
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(CampaignsUiState())
    val state = _state.asStateFlow()

    fun selectTab(status: CampaignStatus) {
        _state.value = _state.value.copy(selectedTab = status)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val byStatus = repos.campaigns.listByStatus(_state.value.selectedTab)
            _state.value =
                CampaignsUiState(
                    selectedTab = _state.value.selectedTab,
                    rows =
                        byStatus.map {
                            CampaignsUiState.CampaignRow(
                                campaignId = it.campaignId.value,
                                startedAt = it.startedAt,
                                lastEventAt = it.lastEventAt,
                                band = it.campaignRiskBand,
                            )
                        },
                    isLoading = false,
                )
        }
    }
}
