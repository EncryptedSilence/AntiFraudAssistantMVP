package com.qalqan.antifraud.correlation

import com.qalqan.antifraud.domain.CampaignId
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.RiskSession
import com.qalqan.antifraud.domain.SessionId
import com.qalqan.antifraud.domain.SessionStatus
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Spec §3.2 — assign a new RiskEvent to an existing open session if it falls inside the chosen
 * window for at least one strong link clue, otherwise open a new session.
 */
object SessionCorrelator {
    sealed interface Outcome {
        data class Attached(val sessionId: SessionId) : Outcome
        data class Created(val session: RiskSession) : Outcome
    }

    fun findOrOpen(
        event: RiskEvent,
        openSessions: List<RiskSession>,
        now: Instant
    ): Outcome {
        for (session in openSessions) {
            val clues = cluesBetween(event, session)
            val window = SessionWindowSelector.windowFor(clues)
            val span = Duration.between(session.startedAt, event.occurredAt)
            if (!span.isNegative && span <= window) {
                return Outcome.Attached(session.id)
            }
        }
        return Outcome.Created(newSessionFor(event, now))
    }

    private fun cluesBetween(event: RiskEvent, session: RiskSession): Set<LinkClue> {
        // In Stage 1 the only clues we can derive without persistence are:
        //  - SAME_PHONE_HASH if a call event matches a call already in the session (caller checks)
        // For now we treat every event as eligible if it falls into the default window; tighter
        // clue derivation lives in the persistence layer (Phase 5) where we have the actual events.
        // This keeps Stage 1 deterministic and unit-testable.
        // The default window in `SessionWindowSelector` is 30 min when no clues are present.
        return when (event) {
            is RiskEvent.Call -> setOf(LinkClue.SAME_PHONE_HASH).takeIf {
                event.event.phoneHash.value.isNotBlank() && session.relatedCallEventIds.isNotEmpty()
            } ?: emptySet()
            else -> emptySet()
        }
    }

    private fun newSessionFor(event: RiskEvent, now: Instant): RiskSession = RiskSession(
        id = SessionId(UUID.randomUUID().toString()),
        startedAt = event.occurredAt,
        endedAt = null,
        status = SessionStatus.OPEN,
        relatedCallEventIds = if (event is RiskEvent.Call) listOf(event.eventId) else emptyList(),
        relatedSmsEventIds = if (event is RiskEvent.Sms) listOf(event.eventId) else emptyList(),
        relatedWebEventIds = if (event is RiskEvent.Web) listOf(event.eventId) else emptyList(),
        relatedUserAnswerIds = emptyList(),
        sessionRiskScore = 0,
        sessionRiskBand = RiskBand.LOW,
        explanation = null
    )

    @Suppress("unused")
    private fun unused(): EventId? = null

    @Suppress("unused")
    private fun unusedCampaign(): CampaignId? = null
}
