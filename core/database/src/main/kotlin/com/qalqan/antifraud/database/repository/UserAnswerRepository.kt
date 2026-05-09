package com.qalqan.antifraud.database.repository

import com.qalqan.antifraud.database.answers.UserAnswerDao
import com.qalqan.antifraud.database.answers.toDomain
import com.qalqan.antifraud.database.answers.toEntity
import com.qalqan.antifraud.domain.AnswerId
import com.qalqan.antifraud.domain.UserAnswer
import java.time.Instant

class UserAnswerRepository internal constructor(private val dao: UserAnswerDao) {
    suspend fun save(answer: UserAnswer) = dao.upsert(answer.toEntity())

    suspend fun find(id: AnswerId): UserAnswer? = dao.findById(id.value)?.toDomain()

    suspend fun listSince(since: Instant): List<UserAnswer> = dao.listSince(since.toEpochMilli()).map { it.toDomain() }

    suspend fun deleteOlderThan(before: Instant): Int = dao.deleteOlderThan(before.toEpochMilli())
}
