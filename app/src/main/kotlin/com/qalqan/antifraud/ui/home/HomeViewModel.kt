package com.qalqan.antifraud.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qalqan.antifraud.calls.BatteryOptimizationPrompt
import com.qalqan.antifraud.calls.CallObserverPermissions
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.CampaignStatus
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.settings.EducationalCardScheduler
import com.qalqan.antifraud.settings.UserSettings
import com.qalqan.antifraud.sms.SmsObserverPermissions
import com.qalqan.antifraud.sync.SyncSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Spec §17.1 — Home-screen state holder. Aggregates §16 entities into a single
 * [HomeUiState] read off the main thread via [viewModelScope].
 */
class HomeViewModel(
    application: Application,
    private val repos: Repositories,
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    @Suppress("LongMethod")
    fun refresh() {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val twentyFourHoursAgo = Instant.now().minusSeconds(SECONDS_PER_DAY)
            val callCount = repos.calls.listSince(twentyFourHoursAgo).size
            val smsCount = repos.sms.listSince(twentyFourHoursAgo).size
            val webCount = repos.web.listSince(twentyFourHoursAgo).size
            val sevenDaysAgo = Instant.now().minusSeconds(SECONDS_PER_DAY * DAYS_PER_WEEK)
            val calls7d = repos.calls.listSince(sevenDaysAgo).size
            val sms7d = repos.sms.listSince(sevenDaysAgo).size
            val web7d = repos.web.listSince(sevenDaysAgo).size
            val campaigns = repos.campaigns.listActive()
            val activeChains =
                campaigns
                    .filter { it.status == CampaignStatus.ACTIVE }
                    .sortedByDescending { it.campaignRiskScore }
                    .map {
                        HomeUiState.ActiveCampaignCard(
                            campaignId = it.campaignId.value,
                            startedAt = it.startedAt,
                            lastEventAt = it.lastEventAt,
                            band = it.campaignRiskBand,
                        )
                    }
            val highest =
                campaigns
                    .filter { it.status == CampaignStatus.ACTIVE }
                    .maxByOrNull { it.campaignRiskScore }
            val alertCount =
                campaigns.count {
                    it.campaignRiskBand == RiskBand.HIGH || it.campaignRiskBand == RiskBand.CRITICAL
                }
            _state.value =
                HomeUiState(
                    currentBand = highest?.campaignRiskBand,
                    eventsLast24h = callCount + smsCount + webCount,
                    alertsLast24h = alertCount,
                    dismissedLast24h = 0,
                    calls24h = callCount,
                    sms24h = smsCount,
                    web24h = webCount,
                    calls7d = calls7d,
                    sms7d = sms7d,
                    web7d = web7d,
                    activeChains = activeChains,
                    activeCampaign =
                        highest?.let {
                            HomeUiState.ActiveCampaignCard(
                                campaignId = it.campaignId.value,
                                startedAt = it.startedAt,
                                lastEventAt = it.lastEventAt,
                                band = it.campaignRiskBand,
                            )
                        },
                    callPermissionState = CallObserverPermissions(app).state(),
                    smsPermissionState = SmsObserverPermissions(app).state(),
                    batteryExempt = BatteryOptimizationPrompt.isExempt(app),
                    syncEnabled = SyncSettings(app).enabled,
                    educationalCardVisible =
                        run {
                            val s = UserSettings(app)
                            EducationalCardScheduler.shouldShow(
                                enabled = s.educationalCardsEnabled,
                                lastShownAtMs = s.lastEducationalCardAtMs,
                                nowMs = System.currentTimeMillis(),
                            )
                        },
                )
        }
    }

    fun dismissEducationalCard() {
        viewModelScope.launch {
            val s = UserSettings(getApplication())
            s.lastEducationalCardAtMs = System.currentTimeMillis()
            refresh()
        }
    }

    private companion object {
        const val SECONDS_PER_DAY: Long = 24L * 60L * 60L
        const val DAYS_PER_WEEK: Long = 7L
    }
}
