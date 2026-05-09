package com.qalqan.antifraud.database.log

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface ApplicationActionLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ApplicationActionLogEntity)

    @Query("SELECT * FROM application_action_log ORDER BY createdAtMs DESC LIMIT :limit")
    suspend fun listRecent(limit: Int): List<ApplicationActionLogEntity>

    @Query("DELETE FROM application_action_log WHERE createdAtMs < :beforeMs")
    suspend fun deleteOlderThan(beforeMs: Long): Int

    @Query("DELETE FROM application_action_log")
    suspend fun deleteAll()
}
