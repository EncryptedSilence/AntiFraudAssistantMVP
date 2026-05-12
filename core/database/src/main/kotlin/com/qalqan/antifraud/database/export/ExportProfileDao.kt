package com.qalqan.antifraud.database.export

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExportProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExportProfileEntity)

    @Query("SELECT * FROM export_profile WHERE exportId = :exportId LIMIT 1")
    suspend fun findById(exportId: String): ExportProfileEntity?

    @Query("SELECT COUNT(*) FROM export_profile")
    suspend fun count(): Int

    @Query("DELETE FROM export_profile")
    suspend fun deleteAll()
}
