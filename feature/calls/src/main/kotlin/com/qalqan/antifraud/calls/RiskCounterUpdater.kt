package com.qalqan.antifraud.calls

import com.qalqan.antifraud.database.repository.ContactProfileRepository
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.ContactProfile
import com.qalqan.antifraud.domain.TrustStatus
import java.time.Instant
import java.util.UUID

/**
 * Spec §16.1 — `ContactProfile.riskCounter` accumulates the number of risk-bearing
 * encounters with this phone hash. Stage 1 added the field; Stage 3 keeps it current
 * for unknown callers so multi-day patterns (§13.3 trust-grooming) can reason over it.
 *
 * Known callers (`isKnownContact = true`) are NOT incremented — the spec treats them
 * as "the user already trusts this number"; bumping their counter would dilute the
 * §12.1 "number not in contacts" signal.
 */
class RiskCounterUpdater(private val contacts: ContactProfileRepository) {
    suspend fun bump(call: CallEvent) {
        if (call.isKnownContact) return
        val now = Instant.now()
        val existing = contacts.findByHash(call.phoneHash)
        val updated = existing?.copy(
            lastSeenAt = now,
            riskCounter = existing.riskCounter + 1,
        ) ?: ContactProfile(
            id = UUID.randomUUID().toString(),
            phoneNormalizedEnc = ByteArray(0),
            phoneHash = call.phoneHash,
            phoneLast4 = null,
            isShortCode = false,
            displayNameLocal = null,
            isInContacts = false,
            trustStatus = TrustStatus.NEUTRAL,
            firstSeenAt = now,
            lastSeenAt = now,
            riskCounter = 1,
            userComment = null,
        )
        contacts.save(updated)
    }
}
