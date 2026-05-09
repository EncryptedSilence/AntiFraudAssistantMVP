package com.qalqan.antifraud.database.sessions

import com.qalqan.antifraud.domain.AnswerId
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.RiskSession
import com.qalqan.antifraud.domain.SessionId
import com.qalqan.antifraud.domain.SessionStatus
import java.time.Instant

internal fun RiskSession.toEntity(): RiskSessionEntity =
    RiskSessionEntity(
        id = id.value,
        startedAtMs = startedAt.toEpochMilli(),
        endedAtMs = endedAt?.toEpochMilli(),
        status = status.name,
        relatedCallEventIds = relatedCallEventIds.map { it.value },
        relatedSmsEventIds = relatedSmsEventIds.map { it.value },
        relatedWebEventIds = relatedWebEventIds.map { it.value },
        relatedUserAnswerIds = relatedUserAnswerIds.map { it.value },
        sessionRiskScore = sessionRiskScore,
        sessionRiskBand = sessionRiskBand.name,
        explanation = explanation,
    )

internal fun RiskSessionEntity.toDomain(): RiskSession =
    RiskSession(
        id = SessionId(id),
        startedAt = Instant.ofEpochMilli(startedAtMs),
        endedAt = endedAtMs?.let(Instant::ofEpochMilli),
        status = SessionStatus.valueOf(status),
        relatedCallEventIds = relatedCallEventIds.map(::EventId),
        relatedSmsEventIds = relatedSmsEventIds.map(::EventId),
        relatedWebEventIds = relatedWebEventIds.map(::EventId),
        relatedUserAnswerIds = relatedUserAnswerIds.map(::AnswerId),
        sessionRiskScore = sessionRiskScore,
        sessionRiskBand = RiskBand.valueOf(sessionRiskBand),
        explanation = explanation,
    )
