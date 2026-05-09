package com.qalqan.antifraud.database.repository

import com.qalqan.antifraud.database.web.WebEventDao
import com.qalqan.antifraud.database.web.toDomain
import com.qalqan.antifraud.database.web.toEntity
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.WebEvent
import java.time.Instant

class WebEventRepository internal constructor(private val dao: WebEventDao) {
    suspend fun save(event: WebEvent) = dao.upsert(event.toEntity())

    suspend fun find(id: EventId): WebEvent? = dao.findById(id.value)?.toDomain()

    suspend fun listSince(since: Instant): List<WebEvent> = dao.listSince(since.toEpochMilli()).map { it.toDomain() }

    suspend fun deleteOlderThan(before: Instant): Int = dao.deleteOlderThan(before.toEpochMilli())
}
