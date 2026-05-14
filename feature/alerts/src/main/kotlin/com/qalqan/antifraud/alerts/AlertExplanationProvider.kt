package com.qalqan.antifraud.alerts

import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.SmsEvent

/**
 * Spec §14 + §23 #17 — at least three specific reasons (or all available reasons if fewer
 * than three exist; for Stage 9 the contract is at-least-three because Stages 1–4 always
 * give the scorer enough signals at HIGH+ to provide three).
 *
 * The provider redacts upstream — it never returns a string containing a phone number,
 * a domain, or an OTP. `AlertContent`'s constructor re-checks (defense in depth).
 *
 * Phase 6 may extend with `reasonsFor(SmsEvent, ...)` overloads. For T13 we ship the call
 * overload; the SMS overload lands at T31 alongside the SMS pipeline wiring.
 */
class AlertExplanationProvider {
    fun reasonsFor(
        event: CallEvent,
        band: RiskBand,
        triggeredPatternLabels: List<String>,
    ): List<String> {
        val lines = mutableListOf<String>()
        if (!event.isKnownContact) {
            lines += "Caller is not in your contacts."
        }
        if (event.direction == CallDirection.INCOMING && event.durationSec >= LONG_CALL_SEC) {
            lines += "Call lasted over a minute."
        }
        if (band == RiskBand.CRITICAL) {
            lines += "Risk level reached critical."
        } else if (band == RiskBand.HIGH) {
            lines += "Risk level reached high."
        }
        triggeredPatternLabels.forEach { label ->
            lines += "Matched pattern: ${sanitize(label)}"
        }
        return lines.take(MAX_LINES).also {
            check(it.size >= MIN_LINES) {
                "expected at least $MIN_LINES reasons; got ${it.size} for band=$band"
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun reasonsFor(
        event: SmsEvent,
        band: RiskBand,
        triggeredPatternLabels: List<String>,
    ): List<String> {
        TODO("SMS overload lands at T31 alongside the SMS pipeline wiring")
    }

    private fun sanitize(label: String): String {
        // strip anything matching the domain pattern so an upstream label like
        // "kaspi-bonus.kz lookalike" becomes "lookalike domain".
        val domain = Regex("""[a-zA-Z0-9-]+\.[a-zA-Z]{2,4}\b""")
        return if (domain.containsMatchIn(label)) {
            domain.replace(label, "lookalike domain").trim()
        } else {
            label
        }
    }

    private companion object {
        const val LONG_CALL_SEC = 60L
        const val MIN_LINES = 3
        const val MAX_LINES = 4
    }
}
