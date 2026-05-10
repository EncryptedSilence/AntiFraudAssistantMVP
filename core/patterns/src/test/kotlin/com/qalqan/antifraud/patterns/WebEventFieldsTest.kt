package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.DomainHash
import com.qalqan.antifraud.domain.DomainStatus
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.WebEvent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class WebEventFieldsTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")
    private val web = RiskEvent.Web(
        WebEvent(
            id = EventId("w1"),
            domainHash = DomainHash("d"),
            domainDisplayLocal = "halykbank.kz",
            visitedAt = t,
            isNewDomain = true,
            domainStatus = DomainStatus.NEW,
            webRiskScore = 35,
            linkedSessionId = null,
            linkedCampaignId = null
        )
    )

    @Test fun `domainDisplayLocal`() { WebEventFields.lookup(web, "domainDisplayLocal") shouldBe "halykbank.kz" }
    @Test fun `isNewDomain`() { WebEventFields.lookup(web, "isNewDomain") shouldBe true }
    @Test fun `domainStatus as String`() { WebEventFields.lookup(web, "domainStatus") shouldBe "NEW" }
    @Test fun `webRiskScore`() { WebEventFields.lookup(web, "webRiskScore") shouldBe 35 }
    @Test fun `unknown field returns null`() { WebEventFields.lookup(web, "ghost") shouldBe null }
}
