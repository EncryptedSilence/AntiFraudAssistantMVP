package com.qalqan.antifraud.database.sessions

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "risk_session",
    indices = [Index("startedAtMs"), Index("status")],
)
internal data class RiskSessionEntity(
    @PrimaryKey val id: String,
    val startedAtMs: Long,
    val endedAtMs: Long?,
    val status: String,
    val relatedCallEventIds: List<String>,
    val relatedSmsEventIds: List<String>,
    val relatedWebEventIds: List<String>,
    val relatedUserAnswerIds: List<String>,
    val sessionRiskScore: Int,
    val sessionRiskBand: String,
    val explanation: String?,
)
