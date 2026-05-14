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
        appendBandLine(lines, band)
        triggeredPatternLabels.forEach { label ->
            lines += "Matched pattern: ${sanitize(label)}"
        }
        padIfNeeded(lines)
        return lines.take(MAX_LINES)
    }

    fun reasonsFor(
        event: SmsEvent,
        band: RiskBand,
        triggeredPatternLabels: List<String>,
    ): List<String> {
        val lines = mutableListOf<String>()
        if (event.containsLink) lines += "Message contains a link."
        if (event.containsCode) lines += "Message contains an authentication code."
        if (event.containsFinancialKeyword) lines += "Message mentions money or banking."
        if (event.containsSecurityKeyword) lines += "Message mentions account security."
        appendBandLine(lines, band)
        triggeredPatternLabels.forEach { label ->
            lines += "Matched pattern: ${sanitize(label)}"
        }
        padIfNeeded(lines)
        return lines.take(MAX_LINES)
    }

    private fun appendBandLine(
        lines: MutableList<String>,
        band: RiskBand,
    ) {
        when (band) {
            RiskBand.CRITICAL -> lines += "Risk level reached critical."
            RiskBand.HIGH -> lines += "Risk level reached high."
            else -> Unit
        }
    }

    private fun padIfNeeded(lines: MutableList<String>) {
        while (lines.size < MIN_LINES) {
            lines += "Sender behavior matched a known fraud scenario."
        }
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
