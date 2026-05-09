package com.qalqan.antifraud.database.contacts

import com.qalqan.antifraud.domain.ContactProfile
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.TrustStatus
import java.time.Instant

internal fun ContactProfile.toEntity(): ContactProfileEntity =
    ContactProfileEntity(
        id = id,
        phoneNormalizedEnc = phoneNormalizedEnc,
        phoneHash = phoneHash.value,
        phoneLast4 = phoneLast4,
        isShortCode = isShortCode,
        displayNameLocal = displayNameLocal,
        isInContacts = isInContacts,
        trustStatus = trustStatus.name,
        firstSeenAtMs = firstSeenAt.toEpochMilli(),
        lastSeenAtMs = lastSeenAt.toEpochMilli(),
        riskCounter = riskCounter,
        userComment = userComment,
    )

internal fun ContactProfileEntity.toDomain(): ContactProfile =
    ContactProfile(
        id = id,
        phoneNormalizedEnc = phoneNormalizedEnc,
        phoneHash = PhoneHash(phoneHash),
        phoneLast4 = phoneLast4,
        isShortCode = isShortCode,
        displayNameLocal = displayNameLocal,
        isInContacts = isInContacts,
        trustStatus = TrustStatus.valueOf(trustStatus),
        firstSeenAt = Instant.ofEpochMilli(firstSeenAtMs),
        lastSeenAt = Instant.ofEpochMilli(lastSeenAtMs),
        riskCounter = riskCounter,
        userComment = userComment,
    )
