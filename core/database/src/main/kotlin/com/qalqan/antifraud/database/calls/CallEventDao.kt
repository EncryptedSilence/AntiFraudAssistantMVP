package com.qalqan.antifraud.database.calls

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface CallEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CallEventEntity)

    @Query("SELECT * FROM call_event WHERE id = :id")
    suspend fun findById(id: String): CallEventEntity?

    @Query("SELECT * FROM call_event WHERE startedAtMs >= :sinceMs ORDER BY startedAtMs ASC")
    suspend fun listSince(sinceMs: Long): List<CallEventEntity>

    @Query("DELETE FROM call_event WHERE startedAtMs < :beforeMs")
    suspend fun deleteOlderThan(beforeMs: Long): Int

    @Query("DELETE FROM call_event")
    suspend fun deleteAll()
}
