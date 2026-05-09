package com.qalqan.antifraud.domain

import java.time.Instant

/**
 * Spec §20.1 — what is logged. Forbidden keys are rejected; the log must never carry phone numbers,
 * SMS bodies, domains, OTPs, or user-typed text.
 */
enum class AppAction {
    APP_START,
    APP_STOP,
    PERMISSION_GRANTED,
    PERMISSION_DENIED,
    SYNC_VERIFY_OK,
    SYNC_VERIFY_FAILED,
    PATTERN_APPLIED,
    PATTERN_ROLLBACK,
    EXPORT,
    DATA_DELETED,
    SETTING_CHANGED,
}

private val FORBIDDEN_DETAIL_KEYS: Set<String> =
    setOf(
        "phoneNumber", "phone", "phoneNormalized",
        "smsBody", "body", "messageBody",
        "domain", "url",
        "otp", "code",
        "userNote", "userText",
    )

data class ApplicationActionLogEntry(
    val id: String,
    val createdAt: Instant,
    val action: AppAction,
    val details: Map<String, String>,
) {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        details.keys.forEach { key ->
            require(key !in FORBIDDEN_DETAIL_KEYS) { "forbidden detail key: $key" }
        }
    }
}
