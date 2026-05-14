package com.qalqan.antifraud.ui.references

import java.time.Instant

/**
 * Spec §17.4 — References screen state. All four tabs are read-only; trusted lists are
 * empty until the user populates them (post-MVP) but are still rendered as section
 * headers when the suspicious side is non-empty.
 */
data class ReferencesUiState(
    val suspiciousNumbers: List<String> = emptyList(),
    val trustedNumbers: List<String> = emptyList(),
    val suspiciousDomains: List<String> = emptyList(),
    val trustedDomains: List<String> = emptyList(),
    val smsCategories: List<String> = emptyList(),
    val officialContacts: List<String> = emptyList(),
    val lastBundleAt: Instant? = null,
    val lastBundleSource: String? = null,
    val isLoading: Boolean = false,
)
