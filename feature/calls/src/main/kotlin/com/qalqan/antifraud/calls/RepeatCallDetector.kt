package com.qalqan.antifraud.calls

import com.qalqan.antifraud.database.repository.CallEventRepository
import com.qalqan.antifraud.domain.PhoneHash
import java.time.Instant

/**
 * Spec §12.1 — "repeated call from unknown number" adds +15 to call-event risk.
 * §10 (correlation) uses a 14-day horizon overall, but the §12.1 repeat-call signal
 * is short-window (24 h): the kind of "they keep calling back" pattern that drives
 * `bank_security_otp_after_call_v1` and friends. Stage 3 implements 24 h; the
 * value can be tightened in Stage 8 sensitivity settings.
 */
class RepeatCallDetector(private val calls: CallEventRepository) {
    suspend fun isRepeated(hash: PhoneHash, now: Instant): Boolean {
        val cutoff = now.minusSeconds(SECONDS_PER_DAY)
        return calls.listSince(cutoff).any { it.phoneHash == hash }
    }

    private companion object {
        const val SECONDS_PER_DAY = 24L * 60L * 60L
    }
}
