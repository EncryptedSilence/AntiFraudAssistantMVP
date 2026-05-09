package com.qalqan.antifraud.database.contacts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ContactProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ContactProfileEntity)

    @Query("SELECT * FROM contact_profile WHERE phoneHash = :phoneHash")
    suspend fun findByHash(phoneHash: String): ContactProfileEntity?

    @Query("SELECT * FROM contact_profile")
    fun observeAll(): Flow<List<ContactProfileEntity>>

    @Query("DELETE FROM contact_profile WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM contact_profile")
    suspend fun deleteAll()
}
