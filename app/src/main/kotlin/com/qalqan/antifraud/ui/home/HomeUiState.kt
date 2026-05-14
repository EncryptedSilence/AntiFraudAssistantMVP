package com.qalqan.antifraud.ui.home

import com.qalqan.antifraud.calls.CallObserverPermissions
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.sms.SmsObserverPermissions
import java.time.Instant

/**
 * Spec §17.1 — Home-screen data shape rendered by [HomeRoute].
 *
 * currentBand == null corresponds to the "All clear" §17.1.1 empty state. activeCampaign
 * is null until there is at least one campaign whose status = ACTIVE and whose
 * lastEventAt is within the §3.3 active-correlation horizon (14 d).
 */
data class HomeUiState(
    val currentBand: RiskBand? = null,
    val eventsLast24h: Int = 0,
    val alertsLast24h: Int = 0,
    val dismissedLast24h: Int = 0,
    val activeCampaign: ActiveCampaignCard? = null,
    val callPermissionState: CallObserverPermissions.State = CallObserverPermissions.State.DENIED,
    val smsPermissionState: SmsObserverPermissions.State = SmsObserverPermissions.State.DENIED,
    val syncEnabled: Boolean = false,
    val batteryExempt: Boolean = false,
    val isLoading: Boolean = false,
) {
    data class ActiveCampaignCard(
        val campaignId: String,
        val startedAt: Instant,
        val lastEventAt: Instant,
        val band: RiskBand,
    )
}
