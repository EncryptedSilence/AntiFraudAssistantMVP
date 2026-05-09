package com.qalqan.antifraud.database.campaigns

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "risk_campaign",
    indices = [Index("lastEventAtMs"), Index("status")],
)
internal data class RiskCampaignEntity(
    @PrimaryKey val campaignId: String,
    val startedAtMs: Long,
    val lastEventAtMs: Long,
    val status: String,
    val scenarioType: String?,
    val relatedPhoneHashes: List<String>,
    val relatedSmsSenderHashes: List<String>,
    val relatedDomainHashes: List<String>,
    val relatedEventIds: List<String>,
    val relatedSessionIds: List<String>,
    val userAnswerIds: List<String>,
    val triggeredPatternIds: List<String>,
    val campaignRiskScore: Int,
    val campaignRiskBand: String,
    val explanation: String?,
)
