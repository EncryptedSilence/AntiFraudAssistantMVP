package com.qalqan.antifraud.database.repository

import com.qalqan.antifraud.database.sms.SmsEventDao
import com.qalqan.antifraud.database.sms.toDomain
import com.qalqan.antifraud.database.sms.toEntity
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.SmsEvent
import java.time.Instant

class SmsEventRepository internal constructor(private val dao: SmsEventDao) {
    suspend fun save(event: SmsEvent) = dao.upsert(event.toEntity())

    suspend fun find(id: EventId): SmsEvent? = dao.findById(id.value)?.toDomain()

    suspend fun listSince(since: Instant): List<SmsEvent> = dao.listSince(since.toEpochMilli()).map { it.toDomain() }

    suspend fun deleteOlderThan(before: Instant): Int = dao.deleteOlderThan(before.toEpochMilli())
}
