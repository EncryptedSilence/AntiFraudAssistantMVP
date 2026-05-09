package com.qalqan.antifraud.database.web

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface WebEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WebEventEntity)

    @Query("SELECT * FROM web_event WHERE id = :id")
    suspend fun findById(id: String): WebEventEntity?

    @Query("SELECT * FROM web_event WHERE visitedAtMs >= :sinceMs ORDER BY visitedAtMs ASC")
    suspend fun listSince(sinceMs: Long): List<WebEventEntity>

    @Query("DELETE FROM web_event WHERE visitedAtMs < :beforeMs")
    suspend fun deleteOlderThan(beforeMs: Long): Int

    @Query("DELETE FROM web_event")
    suspend fun deleteAll()
}
