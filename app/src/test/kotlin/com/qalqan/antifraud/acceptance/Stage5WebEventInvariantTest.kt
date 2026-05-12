package com.qalqan.antifraud.acceptance

import com.qalqan.antifraud.domain.DomainHash
import com.qalqan.antifraud.domain.DomainStatus
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.WebEvent
import io.kotest.assertions.throwables.shouldThrow
import org.junit.Test
import java.time.Instant

/**
 * Spec §16.4 — the `WebEvent.init` invariant rejects any `domainDisplayLocal` that
 * carries a path, query, fragment, or scheme. This is a defense-in-depth re-check that
 * an adversarial caller bypassing [com.qalqan.antifraud.web.DomainNormalizer] would
 * still be blocked at the entity boundary.
 */
class Stage5WebEventInvariantTest {
    @Test
    fun `WebEvent rejects a domainDisplayLocal containing a slash`() {
        shouldThrow<IllegalArgumentException> { buildWith("halykbank.kz/login") }
    }

    @Test
    fun `WebEvent rejects a domainDisplayLocal containing a question mark`() {
        shouldThrow<IllegalArgumentException> { buildWith("halykbank.kz?a=1") }
    }

    @Test
    fun `WebEvent rejects a domainDisplayLocal containing a fragment hash`() {
        shouldThrow<IllegalArgumentException> { buildWith("halykbank.kz#top") }
    }

    @Test
    fun `WebEvent rejects a domainDisplayLocal containing a scheme delimiter`() {
        shouldThrow<IllegalArgumentException> { buildWith("https://halykbank.kz") }
    }

    private fun buildWith(display: String): WebEvent =
        WebEvent(
            id = EventId("00000000-0000-0000-0000-000000000001"),
            domainHash = DomainHash("a".repeat(SHA256_HEX_LEN)),
            domainDisplayLocal = display,
            visitedAt = Instant.now(),
            isNewDomain = true,
            domainStatus = DomainStatus.NEW,
            webRiskScore = 0,
            linkedSessionId = null,
            linkedCampaignId = null,
        )

    private companion object {
        const val SHA256_HEX_LEN = 64
    }
}
