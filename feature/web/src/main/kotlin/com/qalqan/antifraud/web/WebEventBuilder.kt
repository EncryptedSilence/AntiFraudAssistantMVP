package com.qalqan.antifraud.web

import com.qalqan.antifraud.database.manual.WebEntryDigest
import com.qalqan.antifraud.domain.DomainHash
import com.qalqan.antifraud.domain.DomainStatus
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.WebEvent
import java.time.Instant
import java.util.UUID

/**
 * Spec §16.4 — composes the persisted `WebEvent`. `webRiskScore` is left at 0; the
 * `CorrelationOrchestrator` recomputes via `WebBaseRisk.compute(...)` on insert.
 *
 * The builder is intentionally tiny: it does not normalize, does not call the seen-list,
 * does not invoke the lookalike detector. Those are the orchestrator's responsibilities,
 * so this unit can be unit-tested in isolation.
 */
class WebEventBuilder(private val digest: WebEntryDigest) {
    fun build(
        canonical: String,
        visitedAt: Instant,
        isNew: Boolean,
        status: DomainStatus,
    ): WebEvent =
        WebEvent(
            id = EventId(UUID.randomUUID().toString()),
            domainHash = DomainHash(digest.hash(canonical)),
            domainDisplayLocal = canonical,
            visitedAt = visitedAt,
            isNewDomain = isNew,
            domainStatus = status,
            webRiskScore = 0,
            linkedSessionId = null,
            linkedCampaignId = null,
        )
}
