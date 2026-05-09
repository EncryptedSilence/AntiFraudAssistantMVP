package com.qalqan.antifraud.database.log

import com.qalqan.antifraud.domain.AppAction
import com.qalqan.antifraud.domain.ApplicationActionLogEntry
import java.time.Instant
import java.util.UUID

class ApplicationActionLogger internal constructor(private val dao: ApplicationActionLogDao) {
    suspend fun log(
        action: AppAction,
        details: Map<String, String> = emptyMap(),
    ) {
        // Constructing the domain entity validates forbidden keys.
        val entry =
            ApplicationActionLogEntry(
                id = UUID.randomUUID().toString(),
                createdAt = Instant.now(),
                action = action,
                details = details,
            )
        dao.insert(entry.toEntity())
    }
}

class ApplicationActionLogRepository internal constructor(private val dao: ApplicationActionLogDao) {
    suspend fun recent(limit: Int): List<ApplicationActionLogEntry> = dao.listRecent(limit).map { it.toDomain() }

    suspend fun deleteOlderThan(before: Instant): Int = dao.deleteOlderThan(before.toEpochMilli())

    suspend fun deleteAll() = dao.deleteAll()
}
