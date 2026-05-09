package com.qalqan.antifraud.database.web

import com.qalqan.antifraud.domain.DomainHash
import com.qalqan.antifraud.domain.DomainStatus
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.WebEvent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class WebEventMapperTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test
    fun `round trip preserves data`() {
        val ev =
            WebEvent(
                id = EventId("w1"),
                domainHash = DomainHash("h"),
                domainDisplayLocal = "example.com",
                visitedAt = t,
                isNewDomain = true,
                domainStatus = DomainStatus.NEW,
                webRiskScore = SCORE,
                linkedSessionId = null,
                linkedCampaignId = null,
            )
        ev.toEntity().toDomain() shouldBe ev
    }

    private companion object {
        const val SCORE: Int = 25
    }
}
