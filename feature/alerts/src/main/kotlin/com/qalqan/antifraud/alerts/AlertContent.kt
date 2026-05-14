package com.qalqan.antifraud.alerts

/**
 * Spec §17.0.1 — alert copy. Never carries raw phone numbers, domains, SMS bodies, or OTPs.
 * The privacy redaction is asserted at construction time so a careless reason builder cannot
 * accidentally leak. The constructor only enforces *upstream* redaction — it does not redact.
 *
 * Phase 4 uses [title], [reasons], [pauseLabel], [dismissLabel] for the Compose layout;
 * Phase 6 uses the same fields for the heads-up `Notification.style`.
 */
data class AlertContent(
    val reasons: List<String>,
    val title: String = "Possible fraud — pause",
    val pauseLabel: String = "Pause and verify",
    val dismissLabel: String = "Dismiss",
    val whyLinkLabel: String = "Why this alert",
) {
    init {
        require(reasons.size >= MIN_REASONS) {
            "AlertContent needs at least 3 reasons (spec §23 #17); got ${reasons.size}"
        }
        reasons.forEach { line ->
            require(!PHONE_PATTERN.containsMatchIn(line)) {
                "reason contains phone-like digits — must be redacted upstream"
            }
            require(!OTP_PATTERN.containsMatchIn(line)) {
                "reason contains OTP-like digits — must be redacted upstream"
            }
            require(!DOMAIN_PATTERN.containsMatchIn(line)) {
                "reason contains domain-like text — must be redacted upstream"
            }
        }
    }

    companion object {
        const val MIN_REASONS = 3

        // +7DDDDDDDDDDD or 10-15-digit runs.
        private val PHONE_PATTERN = Regex("""\+?\d{10,15}""")

        // 4-6-digit OTP-like sequence.
        private val OTP_PATTERN = Regex("""(?<!\d)\d{4,6}(?!\d)""")

        // very loose: anything ending in a 2-4-char TLD-like suffix.
        private val DOMAIN_PATTERN = Regex("""[a-zA-Z0-9-]+\.[a-zA-Z]{2,4}\b""")
    }
}
