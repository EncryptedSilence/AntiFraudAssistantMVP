package com.qalqan.antifraud.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class WebEventTest {
    private val visited = Instant.parse("2026-05-08T10:00:00Z")

    private fun web(domainDisplay: String = "halykbank.kz", riskScore: Int = 0) = WebEvent(
        id = EventId("e1"),
        domainHash = DomainHash("h"),
        domainDisplayLocal = domainDisplay,
        visitedAt = visited,
        isNewDomain = true,
        domainStatus = DomainStatus.NEW,
        webRiskScore = riskScore,
        linkedSessionId = null,
        linkedCampaignId = null
    )

    @Test fun `domain display must not contain a path or query`() {
        shouldThrow<IllegalArgumentException> { web(domainDisplay = "halykbank.kz/login") }
        shouldThrow<IllegalArgumentException> { web(domainDisplay = "halykbank.kz?id=1") }
        shouldThrow<IllegalArgumentException> { web(domainDisplay = "halykbank.kz#x") }
    }

    @Test fun `domain display must not contain protocol`() {
        shouldThrow<IllegalArgumentException> { web(domainDisplay = "https://halykbank.kz") }
    }

    @Test fun `risk score is bounded to 0 dot dot 100`() {
        shouldThrow<IllegalArgumentException> { web(riskScore = 101) }
    }

    @Test fun `valid web event accepted`() {
        web().domainStatus shouldBe DomainStatus.NEW
    }
}
