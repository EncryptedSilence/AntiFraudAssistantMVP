package com.qalqan.antifraud.domain

import java.time.Instant

/**
 * Spec §16.1 — encrypted-at-rest normalized phone, salted hash for matching, last4 for display fallback.
 */
data class ContactProfile(
    val id: String,
    val phoneNormalizedEnc: ByteArray,
    val phoneHash: PhoneHash,
    val phoneLast4: String?,
    val isShortCode: Boolean,
    val displayNameLocal: String?,
    val isInContacts: Boolean,
    val trustStatus: TrustStatus,
    val firstSeenAt: Instant,
    val lastSeenAt: Instant,
    val riskCounter: Int,
    val userComment: String?
) {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        phoneLast4?.let {
            require(it.length == 4 && it.all(Char::isDigit)) {
                "phoneLast4 must be exactly 4 digits when present"
            }
        }
        require(riskCounter >= 0) { "riskCounter must be non-negative" }
        require(!lastSeenAt.isBefore(firstSeenAt)) {
            "lastSeenAt must be on or after firstSeenAt"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContactProfile) return false
        return id == other.id &&
            phoneNormalizedEnc.contentEquals(other.phoneNormalizedEnc) &&
            phoneHash == other.phoneHash &&
            phoneLast4 == other.phoneLast4 &&
            isShortCode == other.isShortCode &&
            displayNameLocal == other.displayNameLocal &&
            isInContacts == other.isInContacts &&
            trustStatus == other.trustStatus &&
            firstSeenAt == other.firstSeenAt &&
            lastSeenAt == other.lastSeenAt &&
            riskCounter == other.riskCounter &&
            userComment == other.userComment
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + phoneNormalizedEnc.contentHashCode()
        result = 31 * result + phoneHash.hashCode()
        result = 31 * result + (phoneLast4?.hashCode() ?: 0)
        result = 31 * result + isShortCode.hashCode()
        result = 31 * result + (displayNameLocal?.hashCode() ?: 0)
        result = 31 * result + isInContacts.hashCode()
        result = 31 * result + trustStatus.hashCode()
        result = 31 * result + firstSeenAt.hashCode()
        result = 31 * result + lastSeenAt.hashCode()
        result = 31 * result + riskCounter
        result = 31 * result + (userComment?.hashCode() ?: 0)
        return result
    }
}
