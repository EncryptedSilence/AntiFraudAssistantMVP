package com.qalqan.antifraud.database.web

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "web_event",
    indices = [Index("visitedAtMs")],
)
internal data class WebEventEntity(
    @PrimaryKey val id: String,
    val domainHash: String,
    val domainDisplayLocal: String,
    val visitedAtMs: Long,
    val isNewDomain: Boolean,
    val domainStatus: String,
    val webRiskScore: Int,
    val linkedSessionId: String?,
    val linkedCampaignId: String?,
)
