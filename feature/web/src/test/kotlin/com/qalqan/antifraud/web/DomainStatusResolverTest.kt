package com.qalqan.antifraud.web

import com.qalqan.antifraud.domain.DomainStatus
import io.kotest.matchers.shouldBe
import org.junit.Test

class DomainStatusResolverTest {
    private val r = DomainStatusResolver

    @Test
    fun `unseen + no lookalike resolves to NEW`() {
        r.resolve(isNew = true, lookalike = null) shouldBe DomainStatus.NEW
    }

    @Test
    fun `unseen + lookalike resolves to SUSPICIOUS`() {
        r.resolve(isNew = true, lookalike = LookalikeMatch("halykbank.kz", 1)) shouldBe DomainStatus.SUSPICIOUS
    }

    @Test
    fun `seen + no lookalike resolves to KNOWN`() {
        r.resolve(isNew = false, lookalike = null) shouldBe DomainStatus.KNOWN
    }

    @Test
    fun `seen + lookalike still resolves to KNOWN (user already visited, scoring handles the rest)`() {
        // We do not retroactively flip a KNOWN domain to SUSPICIOUS on every revisit.
        // The §12.3 scoring still applies via `WebBaseRisk` regardless of status.
        r.resolve(isNew = false, lookalike = LookalikeMatch("kaspi.kz", 2)) shouldBe DomainStatus.KNOWN
    }
}
