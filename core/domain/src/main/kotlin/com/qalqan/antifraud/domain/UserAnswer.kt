package com.qalqan.antifraud.domain

import java.time.Instant

/**
 * Spec §16.5 — closed enum codes only; optional encrypted user note ≤240 chars.
 * v1's free-form `answerTextLocal` removed: it could have stored OTPs/IDs forbidden by §2.1.
 */
enum class QuestionCode {
    Q1_CALLER_OFFICIAL_CLAIM,
    Q2_PRESSURE_OR_DONT_CONSULT,
    Q3_ASKED_TO_ACT_NOW,
}

enum class AnswerCode { YES, NO, NOT_SURE, NOT_ANSWERED }

data class UserAnswer(
    val id: AnswerId,
    val relatedEventId: EventId,
    val relatedSessionId: SessionId?,
    val relatedCampaignId: CampaignId?,
    val questionCode: QuestionCode,
    val answerCode: AnswerCode,
    val userNoteLocalEnc: ByteArray?,
    val answerRiskScore: Int,
    val createdAt: Instant,
) {
    init {
        require(answerRiskScore in 0..100) { "answerRiskScore must be in 0..100" }
        userNoteLocalEnc?.let {
            require(it.size <= MAX_NOTE_BYTES) { "userNoteLocalEnc exceeds $MAX_NOTE_BYTES bytes" }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserAnswer) return false
        return id == other.id &&
            relatedEventId == other.relatedEventId &&
            relatedSessionId == other.relatedSessionId &&
            relatedCampaignId == other.relatedCampaignId &&
            questionCode == other.questionCode &&
            answerCode == other.answerCode &&
            (userNoteLocalEnc?.contentEquals(other.userNoteLocalEnc) ?: (other.userNoteLocalEnc == null)) &&
            answerRiskScore == other.answerRiskScore &&
            createdAt == other.createdAt
    }

    override fun hashCode(): Int {
        var r = id.hashCode()
        r = 31 * r + relatedEventId.hashCode()
        r = 31 * r + (relatedSessionId?.hashCode() ?: 0)
        r = 31 * r + (relatedCampaignId?.hashCode() ?: 0)
        r = 31 * r + questionCode.hashCode()
        r = 31 * r + answerCode.hashCode()
        r = 31 * r + (userNoteLocalEnc?.contentHashCode() ?: 0)
        r = 31 * r + answerRiskScore
        r = 31 * r + createdAt.hashCode()
        return r
    }

    companion object {
        const val MAX_NOTE_CHARS: Int = 240
        const val MAX_NOTE_BYTES: Int = 768
    }
}
