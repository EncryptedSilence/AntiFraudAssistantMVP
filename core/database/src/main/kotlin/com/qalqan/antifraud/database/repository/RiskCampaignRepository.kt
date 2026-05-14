package com.qalqan.antifraud.database.repository

import com.qalqan.antifraud.database.campaigns.RiskCampaignDao
import com.qalqan.antifraud.database.campaigns.toDomain
import com.qalqan.antifraud.database.campaigns.toEntity
import com.qalqan.antifraud.domain.CampaignId
import com.qalqan.antifraud.domain.CampaignStatus
import com.qalqan.antifraud.domain.RiskCampaign
import java.time.Instant

class RiskCampaignRepository internal constructor(private val dao: RiskCampaignDao) {
    suspend fun save(campaign: RiskCampaign) = dao.upsert(campaign.toEntity())

    suspend fun find(id: CampaignId): RiskCampaign? = dao.findById(id.value)?.toDomain()

    suspend fun findById(id: String): RiskCampaign? = dao.findById(id)?.toDomain()

    suspend fun listActive(): List<RiskCampaign> = dao.listActive().map { it.toDomain() }

    suspend fun listByStatus(status: CampaignStatus): List<RiskCampaign> =
        dao.listByStatus(status.name).map { it.toDomain() }

    suspend fun listAll(): List<RiskCampaign> = dao.listAll().map { it.toDomain() }

    suspend fun markFalsePositive(id: CampaignId): Int = dao.markFalsePositive(id.value)

    suspend fun updateStatus(id: String, status: CampaignStatus): Int = dao.updateStatus(id, status.name)

    suspend fun deleteArchivedOlderThan(before: Instant): Int = dao.deleteArchivedOlderThan(before.toEpochMilli())
}
