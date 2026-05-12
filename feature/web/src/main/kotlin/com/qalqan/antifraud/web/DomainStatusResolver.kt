package com.qalqan.antifraud.web

import com.qalqan.antifraud.domain.DomainStatus

/**
 * Spec §5.4 — maps the two boolean signals Stage 5 produces (is the hash new?
 * does the canonical look like a known bank?) into the `DomainStatus` enum that
 * `WebEvent` persists. The fuller status set (`TRUSTED`, `IGNORED`, `BLOCKED`) is
 * user-curated and lives in Stage 8's domain-detail screen — the orchestrator
 * never assigns those values on its own.
 */
object DomainStatusResolver {
    fun resolve(
        isNew: Boolean,
        lookalike: LookalikeMatch?,
    ): DomainStatus =
        when {
            isNew && lookalike != null -> DomainStatus.SUSPICIOUS
            isNew -> DomainStatus.NEW
            else -> DomainStatus.KNOWN
        }
}
