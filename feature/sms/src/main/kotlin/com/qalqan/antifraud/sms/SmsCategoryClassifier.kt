package com.qalqan.antifraud.sms

import com.qalqan.antifraud.database.manual.OtpAndIdGuard
import com.qalqan.antifraud.domain.SmsCategory

/**
 * Spec §5.3 — maps a parsed SMS to one of the `SmsCategory` enum values using local rules.
 *
 * Priority order (first match wins):
 *  1. AUTHORITY_SHORTCODE — sender is in the short-code authority list (1414, 112).
 *  2. SECURITY_WARNING    — body contains security-domain keywords.
 *  3. BANK                — body contains financial keywords.
 *  4. OTP                 — body contains a 4–6 digit code (via existing OtpAndIdGuard).
 *  5. LINK                — body contains an http(s) URL but no stronger signal.
 *  6. UNKNOWN_SENDER      — fall-through default; the Stage 8 sensitivity-settings UX may
 *                          tighten this later.
 *
 * LOGIN / REGISTRATION / PASSWORD_CHANGE / TRANSFER / LOAN are reserved enum values that
 * Stage 4 leaves classifier-blank; they are still expressible by user-pattern rules and by
 * the post-MVP §19.16 format-authenticity module.
 */
object SmsCategoryClassifier {

    fun classify(rawSender: String, body: String): SmsCategory =
        when {
            SmsKeywordDetector.isAuthorityShortCode(rawSender) -> SmsCategory.AUTHORITY_SHORTCODE
            SmsKeywordDetector.containsSecurityKeyword(body) -> SmsCategory.SECURITY_WARNING
            SmsKeywordDetector.containsFinancialKeyword(body) -> SmsCategory.BANK
            OtpAndIdGuard.isLikelySensitive(body) -> SmsCategory.OTP
            SmsKeywordDetector.containsLink(body) -> SmsCategory.LINK
            else -> SmsCategory.UNKNOWN_SENDER
        }
}
