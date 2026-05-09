package com.qalqan.antifraud.database.answers

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_answer",
    indices = [Index("createdAtMs"), Index("relatedEventId")],
)
internal data class UserAnswerEntity(
    @PrimaryKey val id: String,
    val relatedEventId: String,
    val relatedSessionId: String?,
    val relatedCampaignId: String?,
    val questionCode: String,
    val answerCode: String,
    val userNoteLocalEnc: ByteArray?,
    val answerRiskScore: Int,
    val createdAtMs: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserAnswerEntity) return false
        return id == other.id &&
            relatedEventId == other.relatedEventId &&
            relatedSessionId == other.relatedSessionId &&
            relatedCampaignId == other.relatedCampaignId &&
            questionCode == other.questionCode &&
            answerCode == other.answerCode &&
            (userNoteLocalEnc?.contentEquals(other.userNoteLocalEnc) ?: (other.userNoteLocalEnc == null)) &&
            answerRiskScore == other.answerRiskScore &&
            createdAtMs == other.createdAtMs
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
        r = 31 * r + createdAtMs.hashCode()
        return r
    }
}
