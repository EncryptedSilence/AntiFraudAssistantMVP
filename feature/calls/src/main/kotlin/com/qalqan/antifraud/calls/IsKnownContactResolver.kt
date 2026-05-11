package com.qalqan.antifraud.calls

import com.qalqan.antifraud.database.repository.ContactProfileRepository
import com.qalqan.antifraud.domain.PhoneHash

/**
 * Spec §5.1 — a phone is "known" when a ContactProfile exists for the salted hash AND
 * the profile is marked `isInContacts`. A profile may exist with `isInContacts = false`
 * for a number the user has only labeled (e.g., suspicious) but never added to contacts.
 */
class IsKnownContactResolver(private val contacts: ContactProfileRepository) {
    suspend fun isKnown(hash: PhoneHash): Boolean = contacts.findByHash(hash)?.isInContacts == true
}
