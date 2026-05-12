package com.qalqan.antifraud.export

/**
 * Spec §8.4 — anonymization options applied to [ExportRecord]s before formatting. Stage 7
 * ships exactly the three the spec marks demo-mandatory; the remaining six options
 * (hide-numbers-entirely, hashed-numbers, hide-domains, remove-SMS-text,
 * remove-user-comments, remove-exact-times) are post-MVP and add variants here as their
 * UI affordances are built out.
 *
 * Implemented as a sealed interface so callers can enumerate the operational set
 * exhaustively. `jsonValue` is the on-disk identifier persisted to
 * `ExportProfile.anonymizationLevel`.
 */
sealed interface AnonymizationOption {
    val jsonValue: String

    data object NumbersLast4 : AnonymizationOption {
        override val jsonValue: String = "numbers_last_4"
    }

    data object DomainZoneOnly : AnonymizationOption {
        override val jsonValue: String = "domain_zone_only"
    }

    data object DatesDayOnly : AnonymizationOption {
        override val jsonValue: String = "dates_day_only"
    }

    companion object {
        /** Spec §8.4 demo-mandatory subset. Stage 7's only operational set. */
        val OPERATIONAL: Set<AnonymizationOption> = setOf(NumbersLast4, DomainZoneOnly, DatesDayOnly)
    }
}
