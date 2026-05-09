package com.qalqan.antifraud.database.campaigns

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface RiskCampaignDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RiskCampaignEntity)

    @Query("SELECT * FROM risk_campaign WHERE campaignId = :id")
    suspend fun findById(id: String): RiskCampaignEntity?

    @Query("SELECT * FROM risk_campaign WHERE status = 'ACTIVE' ORDER BY lastEventAtMs ASC")
    suspend fun listActive(): List<RiskCampaignEntity>

    @Query("SELECT * FROM risk_campaign ORDER BY lastEventAtMs ASC")
    suspend fun listAll(): List<RiskCampaignEntity>

    @Query("DELETE FROM risk_campaign WHERE status = 'ARCHIVED' AND lastEventAtMs < :beforeMs")
    suspend fun deleteArchivedOlderThan(beforeMs: Long): Int

    @Query("UPDATE risk_campaign SET status = 'FALSE_POSITIVE' WHERE campaignId = :id")
    suspend fun markFalsePositive(id: String): Int

    @Query("DELETE FROM risk_campaign")
    suspend fun deleteAll()
}
