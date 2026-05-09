package com.qalqan.antifraud.database.answers

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface UserAnswerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserAnswerEntity)

    @Query("SELECT * FROM user_answer WHERE id = :id")
    suspend fun findById(id: String): UserAnswerEntity?

    @Query("SELECT * FROM user_answer WHERE createdAtMs >= :sinceMs ORDER BY createdAtMs ASC")
    suspend fun listSince(sinceMs: Long): List<UserAnswerEntity>

    /**
     * Spec §2.1 — projection that intentionally omits [UserAnswerEntity.userNoteLocalEnc] to
     * prove the encrypted bytes never leak into a plain text view by accident.
     */
    @Query("SELECT id, questionCode, answerCode FROM user_answer")
    suspend fun listMinimalProjection(): List<UserAnswerSummary>

    @Query("DELETE FROM user_answer WHERE createdAtMs < :beforeMs")
    suspend fun deleteOlderThan(beforeMs: Long): Int

    @Query("DELETE FROM user_answer")
    suspend fun deleteAll()
}

internal data class UserAnswerSummary(
    val id: String,
    val questionCode: String,
    val answerCode: String,
)
