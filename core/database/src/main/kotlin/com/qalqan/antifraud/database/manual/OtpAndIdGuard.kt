package com.qalqan.antifraud.database.manual

/**
 * Spec §16.5 — note field rejects OTP-shaped strings before they reach the database.
 *
 * Detection rules (conservative — false positive over false negative):
 *   - any all-digit substring of length 4..6 → likely OTP
 *   - any all-digit substring of length 12 → likely IIN (KZ ID)
 *   - any digit-and-space substring of length 16 (after removing spaces) → likely card
 */
object OtpAndIdGuard {
    private val OTP = Regex("(?<!\\d)\\d{4,6}(?!\\d)")
    private val IIN = Regex("(?<!\\d)\\d{12}(?!\\d)")
    private val CARD = Regex("(?<!\\d)\\d(?:\\s?\\d){15}(?!\\d)")

    fun isLikelySensitive(note: String): Boolean {
        val trimmed = note.trim()
        return OTP.containsMatchIn(trimmed) ||
            IIN.containsMatchIn(trimmed) ||
            CARD.containsMatchIn(trimmed)
    }
}
