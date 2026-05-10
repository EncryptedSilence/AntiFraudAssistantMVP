package com.qalqan.antifraud.database.patterns

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface PatternStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PatternStateEntity)

    @Query("SELECT * FROM pattern_state WHERE pattern_id = :patternId LIMIT 1")
    suspend fun findById(patternId: String): PatternStateEntity?

    @Query("SELECT * FROM pattern_state")
    suspend fun listAll(): List<PatternStateEntity>

    @Query("DELETE FROM pattern_state")
    suspend fun deleteAll()
}
