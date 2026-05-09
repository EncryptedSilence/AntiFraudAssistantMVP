package com.qalqan.antifraud.database.contacts

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_profile")
internal data class ContactProfileEntity(
    @PrimaryKey val id: String,
    val phoneNormalizedEnc: ByteArray,
    val phoneHash: String,
    val phoneLast4: String?,
    val isShortCode: Boolean,
    val displayNameLocal: String?,
    val isInContacts: Boolean,
    val trustStatus: String,
    val firstSeenAtMs: Long,
    val lastSeenAtMs: Long,
    val riskCounter: Int,
    val userComment: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContactProfileEntity) return false
        return id == other.id &&
            phoneNormalizedEnc.contentEquals(other.phoneNormalizedEnc) &&
            phoneHash == other.phoneHash &&
            phoneLast4 == other.phoneLast4 &&
            isShortCode == other.isShortCode &&
            displayNameLocal == other.displayNameLocal &&
            isInContacts == other.isInContacts &&
            trustStatus == other.trustStatus &&
            firstSeenAtMs == other.firstSeenAtMs &&
            lastSeenAtMs == other.lastSeenAtMs &&
            riskCounter == other.riskCounter &&
            userComment == other.userComment
    }

    override fun hashCode(): Int {
        var r = id.hashCode()
        r = 31 * r + phoneNormalizedEnc.contentHashCode()
        r = 31 * r + phoneHash.hashCode()
        r = 31 * r + (phoneLast4?.hashCode() ?: 0)
        r = 31 * r + isShortCode.hashCode()
        r = 31 * r + (displayNameLocal?.hashCode() ?: 0)
        r = 31 * r + isInContacts.hashCode()
        r = 31 * r + trustStatus.hashCode()
        r = 31 * r + firstSeenAtMs.hashCode()
        r = 31 * r + lastSeenAtMs.hashCode()
        r = 31 * r + riskCounter
        r = 31 * r + (userComment?.hashCode() ?: 0)
        return r
    }
}
