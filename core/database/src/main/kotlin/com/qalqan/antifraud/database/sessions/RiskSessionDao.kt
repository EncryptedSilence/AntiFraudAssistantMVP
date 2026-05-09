package com.qalqan.antifraud.database.sessions

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface RiskSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RiskSessionEntity)

    @Query("SELECT * FROM risk_session WHERE id = :id")
    suspend fun findById(id: String): RiskSessionEntity?

    @Query("SELECT * FROM risk_session WHERE status = 'OPEN' ORDER BY startedAtMs ASC")
    suspend fun listOpen(): List<RiskSessionEntity>

    @Query("SELECT * FROM risk_session ORDER BY startedAtMs ASC")
    suspend fun listAll(): List<RiskSessionEntity>

    @Query("DELETE FROM risk_session WHERE startedAtMs < :beforeMs")
    suspend fun deleteOlderThan(beforeMs: Long): Int

    @Query("DELETE FROM risk_session")
    suspend fun deleteAll()
}
