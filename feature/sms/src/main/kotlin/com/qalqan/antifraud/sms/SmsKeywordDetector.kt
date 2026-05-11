package com.qalqan.antifraud.sms

/**
 * Spec §5.3 — local on-device keyword tables (no AI, no remote lookup). The lists are
 * intentionally short and conservative; Stage 8 sensitivity settings may tune them.
 *
 * §5.1 — Kazakhstan short codes (1414, 112) are flagged at this layer; the §19.16
 * format-authenticity module (post-MVP) extends the catalog.
 */
object SmsKeywordDetector {
    private val FINANCIAL_TERMS: List<String> =
        listOf(
            // KZ Russian
            "перевод", "списание", "зачислен", "карта", "счет", "счёт",
            "сумма", "kzt", "тенге", "kaspi", "halyk", "сбербанк",
            // English
            "transfer", "withdrawn", "deposit", "balance", "card", "account",
        )

    private val SECURITY_TERMS: List<String> =
        listOf(
            // KZ Russian
            "подозрительн", "безопасн", "вход", "код подтверждения",
            // English
            "suspicious", "security", "sign-in", "sign in", "verification code",
        )

    private val AUTHORITY_SHORT_CODES: Set<String> = setOf("1414", "112")

    fun containsFinancialKeyword(body: String): Boolean = FINANCIAL_TERMS.any { body.contains(it, ignoreCase = true) }

    fun containsSecurityKeyword(body: String): Boolean = SECURITY_TERMS.any { body.contains(it, ignoreCase = true) }

    fun isAuthorityShortCode(rawSender: String): Boolean = rawSender.trim() in AUTHORITY_SHORT_CODES

    fun containsLink(body: String): Boolean =
        body.contains("http://", ignoreCase = true) ||
            body.contains("https://", ignoreCase = true)
}
