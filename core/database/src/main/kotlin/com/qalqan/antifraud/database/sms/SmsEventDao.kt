package com.qalqan.antifraud.database.sms

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface SmsEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SmsEventEntity)

    @Query("SELECT * FROM sms_event WHERE id = :id")
    suspend fun findById(id: String): SmsEventEntity?

    @Query("SELECT * FROM sms_event WHERE receivedAtMs >= :sinceMs ORDER BY receivedAtMs ASC")
    suspend fun listSince(sinceMs: Long): List<SmsEventEntity>

    @Query("DELETE FROM sms_event WHERE receivedAtMs < :beforeMs")
    suspend fun deleteOlderThan(beforeMs: Long): Int

    @Query("DELETE FROM sms_event")
    suspend fun deleteAll()
}
