package com.qalqan.antifraud.scoring

import com.qalqan.antifraud.domain.DomainHash
import com.qalqan.antifraud.domain.DomainStatus
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.WebEvent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class WebBaseRiskTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    private fun web(
        isNew: Boolean = true,
        domain: String = "halykbank.kz"
    ) = WebEvent(
        id = EventId("w"),
        domainHash = DomainHash("h"),
        domainDisplayLocal = domain,
        visitedAt = t,
        isNewDomain = isNew,
        domainStatus = if (isNew) DomainStatus.NEW else DomainStatus.KNOWN,
        webRiskScore = 0,
        linkedSessionId = null,
        linkedCampaignId = null
    )

    @Test fun `new domain contributes 10`() {
        WebBaseRisk.compute(web(isNew = true), lookalikeMatch = false) shouldBe 10
    }

    @Test fun `known domain contributes 0`() {
        WebBaseRisk.compute(web(isNew = false), lookalikeMatch = false) shouldBe 0
    }

    @Test fun `lookalike match adds 35`() {
        WebBaseRisk.compute(web(isNew = true), lookalikeMatch = true) shouldBe 45
    }
}
