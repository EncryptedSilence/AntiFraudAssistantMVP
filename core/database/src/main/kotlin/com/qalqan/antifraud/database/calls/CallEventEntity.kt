package com.qalqan.antifraud.database.calls

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "call_event",
    indices = [Index("startedAtMs"), Index("phoneHash")],
)
internal data class CallEventEntity(
    @PrimaryKey val id: String,
    val phoneHash: String,
    val simSlot: Int?,
    val direction: String,
    val startedAtMs: Long,
    val endedAtMs: Long?,
    val durationSec: Long,
    val isKnownContact: Boolean,
    val isRepeated: Boolean,
    val callRiskScore: Int,
    val linkedSessionId: String?,
    val linkedCampaignId: String?,
)
