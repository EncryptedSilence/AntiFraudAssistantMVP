package com.qalqan.antifraud.export

/**
 * Spec §8.2 — the three export categories Stage 7 ships. The remaining §8.2 categories
 * (trusted phones / domains, sessions, user-created patterns, warning log, user answers,
 * sensitivity settings, local category dictionary) are deferred to later stages — the
 * pipeline is forward-compatible: adding a category means adding a variant here, an
 * [ExportRecord] sub-variant, and a gatherer arm.
 *
 * `jsonValue` is the on-disk identifier used by the JSON / CSV formatters and the
 * `ExportProfile.exportType` field; the enum NAME stays Kotlin-idiomatic UPPER_SNAKE.
 */
enum class ExportCategory(val jsonValue: String) {
    SUSPICIOUS_NUMBERS("suspicious_numbers"),
    RISK_CAMPAIGNS("risk_campaigns"),
    TRIGGERED_PATTERNS("triggered_patterns"),
}
