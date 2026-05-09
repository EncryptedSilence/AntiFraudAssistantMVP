package com.qalqan.antifraud.database.repository

import com.qalqan.antifraud.database.calls.CallEventDao
import com.qalqan.antifraud.database.calls.toDomain
import com.qalqan.antifraud.database.calls.toEntity
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import java.time.Instant

class CallEventRepository internal constructor(private val dao: CallEventDao) {
    suspend fun save(event: CallEvent) = dao.upsert(event.toEntity())

    suspend fun find(id: EventId): CallEvent? = dao.findById(id.value)?.toDomain()

    suspend fun listSince(since: Instant): List<CallEvent> = dao.listSince(since.toEpochMilli()).map { it.toDomain() }

    suspend fun deleteOlderThan(before: Instant): Int = dao.deleteOlderThan(before.toEpochMilli())
}
