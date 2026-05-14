package com.qalqan.antifraud.ui.campaign

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.CampaignStatus
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.patterns.BatchPatternMatcher
import com.qalqan.antifraud.patterns.PatternExplainer
import com.qalqan.antifraud.patterns.SeedPatternLoader
import com.qalqan.antifraud.settings.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Spec §17.2 — ViewModel for the Campaign detail screen. Reads the selected campaign,
 * collects the linked events, runs them through the pattern catalog, and derives
 * §17.2 reasons via [PatternExplainer.explain].
 */
class CampaignDetailViewModel(
    application: Application,
    private val repos: Repositories,
    private val userSettings: UserSettings,
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(CampaignDetailUiState())
    val state = _state.asStateFlow()

    fun load(campaignId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, campaignId = campaignId)
            val campaign = repos.campaigns.findById(campaignId)
            if (campaign == null) {
                _state.value =
                    CampaignDetailUiState(
                        campaignId = campaignId,
                        advancedRulesEnabled = userSettings.advancedRulesEnabled,
                    )
                return@launch
            }
            val ids = campaign.relatedEventIds.map { it.value }.toSet()
            val events = collectCampaignEvents(ids)
            val patterns =
                SeedPatternLoader.load().filter { p ->
                    repos.patternState.isEnabled(p.patternId.value, default = p.enabled)
                }
            val matchResults = BatchPatternMatcher.matchAll(patterns, events)
            val triggeredPairs = patterns.zip(matchResults).filter { (_, r) -> r.matched }
            val reasons =
                if (triggeredPairs.isNotEmpty()) {
                    PatternExplainer.explain(triggeredPairs).reasons.map { it.text }
                } else {
                    emptyList()
                }
            _state.value =
                CampaignDetailUiState(
                    campaignId = campaign.campaignId.value,
                    startedAt = campaign.startedAt,
                    lastEventAt = campaign.lastEventAt,
                    band = campaign.campaignRiskBand,
                    linkedEvents =
                        events.map { event ->
                            "${event.javaClass.simpleName} @ ${event.occurredAt}"
                        },
                    triggeredPatterns = triggeredPairs.map { (p, _) -> p.name },
                    reasons = reasons,
                    pendingQuestions = emptyList(),
                    recommendations = emptyList(),
                    advancedRulesEnabled = userSettings.advancedRulesEnabled,
                    isLoading = false,
                )
        }
    }

    private suspend fun collectCampaignEvents(ids: Set<String>): List<RiskEvent> {
        if (ids.isEmpty()) return emptyList()
        val calls = repos.calls.listSince(Instant.EPOCH).filter { it.id.value in ids }
        val sms = repos.sms.listSince(Instant.EPOCH).filter { it.id.value in ids }
        val web = repos.web.listSince(Instant.EPOCH).filter { it.id.value in ids }
        return calls.map { RiskEvent.Call(it) } +
            sms.map { RiskEvent.Sms(it) } +
            web.map { RiskEvent.Web(it) }
    }

    fun closeCampaign() {
        viewModelScope.launch {
            val id = _state.value.campaignId
            if (id.isNotBlank()) {
                repos.campaigns.updateStatus(id, CampaignStatus.CLOSED)
                load(id)
            }
        }
    }

    fun markFalseAlarm() {
        viewModelScope.launch {
            val id = _state.value.campaignId
            if (id.isNotBlank()) {
                repos.campaigns.updateStatus(id, CampaignStatus.FALSE_POSITIVE)
                load(id)
            }
        }
    }
}
