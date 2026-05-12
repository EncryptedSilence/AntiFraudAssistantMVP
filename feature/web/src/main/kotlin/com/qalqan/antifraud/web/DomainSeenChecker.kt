package com.qalqan.antifraud.web

import com.qalqan.antifraud.database.repository.WebEventRepository
import java.time.Instant

/**
 * Spec §5.4 — decides whether a `domainHash` has been recorded before, so the
 * orchestrator can set `WebEvent.isNewDomain` correctly. The horizon is the full
 * persisted seen-list (we use `Instant.EPOCH` as the lower bound) because §15
 * retention has already purged anything older than the configured horizon.
 *
 * The current `WebEventRepository` API does not expose a direct hash lookup; we
 * walk `listSince(Instant.EPOCH)` instead. With Stage 5 manual-only traffic that is
 * trivially small. If web volume grows, an index + dedicated `existsByHash` method
 * can land in Stage 8 without touching this caller.
 */
class DomainSeenChecker(private val web: WebEventRepository) {
    suspend fun isNew(hashHex: String): Boolean = web.listSince(Instant.EPOCH).none { it.domainHash.value == hashHex }
}
