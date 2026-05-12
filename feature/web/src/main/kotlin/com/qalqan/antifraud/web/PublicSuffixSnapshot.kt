package com.qalqan.antifraud.web

/**
 * Spec §5.4 / §16.4 — extract eTLD+1 from user-typed input.
 *
 * A hand-curated minimal snapshot of the Mozilla Public Suffix List, focused on the
 * top-level domains Stage 5 cares about: Kazakhstan (.kz family), neighbor TLDs that
 * KZ users frequently visit (.kg, .ru, .uz), and the common gTLDs (.com, .org, .net,
 * .io). Sorted longest-first so [longestMatch] finds the most-specific suffix.
 *
 * Why not import the full PSL: ~15 K lines, ~250 KB of APK weight. Stage 5 is a demo;
 * a missing TLD falls through to a "treat the rightmost two labels as eTLD+1" default
 * that matches the PSL's own behavior for unknown TLDs.
 *
 * Stage 6 sync may later refresh this list from a signed update package; the read path
 * is the same.
 */
object PublicSuffixSnapshot {
    val suffixes: List<String> =
        listOf(
            // KZ — most-specific first
            "kgd.gov.kz",
            "gov.kz",
            "edu.kz",
            "com.kz",
            "org.kz",
            "net.kz",
            "kz",
            // Neighbor TLDs
            "kg",
            "ru",
            "uz",
            // Common gTLDs Kazakhstani users land on
            "com",
            "org",
            "net",
            "io",
        )

    /**
     * Returns the longest suffix that matches the trailing labels of [host], or `null` when none
     * of the curated entries match. Caller falls back to the rightmost two labels in that case.
     */
    fun longestMatch(host: String): String? {
        val lower = host.lowercase()
        return suffixes.firstOrNull { suffix ->
            lower == suffix || lower.endsWith(".$suffix")
        }
    }
}
