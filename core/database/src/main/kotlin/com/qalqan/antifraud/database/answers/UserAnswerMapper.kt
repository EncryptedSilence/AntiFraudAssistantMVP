package com.qalqan.antifraud.database.answers

import com.qalqan.antifraud.domain.AnswerCode
import com.qalqan.antifraud.domain.AnswerId
import com.qalqan.antifraud.domain.CampaignId
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.QuestionCode
import com.qalqan.antifraud.domain.SessionId
import com.qalqan.antifraud.domain.UserAnswer
import java.time.Instant

internal fun UserAnswer.toEntity(): UserAnswerEntity =
    UserAnswerEntity(
        id = id.value,
        relatedEventId = relatedEventId.value,
        relatedSessionId = relatedSessionId?.value,
        relatedCampaignId = relatedCampaignId?.value,
        questionCode = questionCode.name,
        answerCode = answerCode.name,
        userNoteLocalEnc = userNoteLocalEnc,
        answerRiskScore = answerRiskScore,
        createdAtMs = createdAt.toEpochMilli(),
    )

internal fun UserAnswerEntity.toDomain(): UserAnswer =
    UserAnswer(
        id = AnswerId(id),
        relatedEventId = EventId(relatedEventId),
        relatedSessionId = relatedSessionId?.let(::SessionId),
        relatedCampaignId = relatedCampaignId?.let(::CampaignId),
        questionCode = QuestionCode.valueOf(questionCode),
        answerCode = AnswerCode.valueOf(answerCode),
        userNoteLocalEnc = userNoteLocalEnc,
        answerRiskScore = answerRiskScore,
        createdAt = Instant.ofEpochMilli(createdAtMs),
    )
