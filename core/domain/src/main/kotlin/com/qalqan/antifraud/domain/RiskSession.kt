package com.qalqan.antifraud.domain

import java.time.Instant

/**
 * Spec §16.6 + §3.2 — short-window grouping of related events.
 */
data class RiskSession(
    val id: SessionId,
    val startedAt: Instant,
    val endedAt: Instant?,
    val status: SessionStatus,
    val relatedCallEventIds: List<EventId>,
    val relatedSmsEventIds: List<EventId>,
    val relatedWebEventIds: List<EventId>,
    val relatedUserAnswerIds: List<AnswerId>,
    val sessionRiskScore: Int,
    val sessionRiskBand: RiskBand,
    val explanation: String?
) {
    init {
        endedAt?.let {
            require(!it.isBefore(startedAt)) { "endedAt must be on or after startedAt" }
        }
        require(sessionRiskScore in 0..100) { "sessionRiskScore must be in 0..100" }
    }
}
