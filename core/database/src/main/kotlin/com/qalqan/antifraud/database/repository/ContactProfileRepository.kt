package com.qalqan.antifraud.database.repository

import com.qalqan.antifraud.database.contacts.ContactProfileDao
import com.qalqan.antifraud.database.contacts.toDomain
import com.qalqan.antifraud.database.contacts.toEntity
import com.qalqan.antifraud.domain.ContactProfile
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.TrustStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContactProfileRepository internal constructor(private val dao: ContactProfileDao) {
    suspend fun save(profile: ContactProfile) = dao.upsert(profile.toEntity())

    suspend fun findByHash(hash: PhoneHash): ContactProfile? = dao.findByHash(hash.value)?.toDomain()

    fun observeAll(): Flow<List<ContactProfile>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun listAll(): List<ContactProfile> = dao.listAll().map { it.toDomain() }

    suspend fun updateTrustStatus(
        id: String,
        status: TrustStatus,
    ) = dao.updateTrustStatus(id, status.name)

    suspend fun deleteById(id: String) = dao.deleteById(id)
}
