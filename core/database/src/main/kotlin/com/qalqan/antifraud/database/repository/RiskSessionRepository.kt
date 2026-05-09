package com.qalqan.antifraud.database.repository

import com.qalqan.antifraud.database.sessions.RiskSessionDao
import com.qalqan.antifraud.database.sessions.toDomain
import com.qalqan.antifraud.database.sessions.toEntity
import com.qalqan.antifraud.domain.RiskSession
import com.qalqan.antifraud.domain.SessionId
import java.time.Instant

class RiskSessionRepository internal constructor(private val dao: RiskSessionDao) {
    suspend fun save(session: RiskSession) = dao.upsert(session.toEntity())

    suspend fun find(id: SessionId): RiskSession? = dao.findById(id.value)?.toDomain()

    suspend fun listOpen(): List<RiskSession> = dao.listOpen().map { it.toDomain() }

    suspend fun listAll(): List<RiskSession> = dao.listAll().map { it.toDomain() }

    suspend fun deleteOlderThan(before: Instant): Int = dao.deleteOlderThan(before.toEpochMilli())
}
