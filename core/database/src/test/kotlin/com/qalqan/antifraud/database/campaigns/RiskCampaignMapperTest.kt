package com.qalqan.antifraud.database.campaigns

import com.qalqan.antifraud.domain.AnswerId
import com.qalqan.antifraud.domain.CampaignId
import com.qalqan.antifraud.domain.CampaignStatus
import com.qalqan.antifraud.domain.DomainHash
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PatternId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.RiskCampaign
import com.qalqan.antifraud.domain.ScenarioCategory
import com.qalqan.antifraud.domain.SenderHash
import com.qalqan.antifraud.domain.SessionId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class RiskCampaignMapperTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test
    fun `round trip preserves all collection columns`() {
        val c =
            RiskCampaign(
                campaignId = CampaignId("k1"),
                startedAt = t,
                lastEventAt = t.plusSeconds(SIXTY),
                status = CampaignStatus.ACTIVE,
                scenarioType = ScenarioCategory.BANK_FRAUD,
                relatedPhoneHashes = setOf(PhoneHash("p1"), PhoneHash("p2")),
                relatedSmsSenderHashes = setOf(SenderHash("s1")),
                relatedDomainHashes = setOf(DomainHash("d1")),
                relatedEventIds = listOf(EventId("e1")),
                relatedSessionIds = listOf(SessionId("s1")),
                userAnswerIds = listOf(AnswerId("a1")),
                triggeredPatternIds = listOf(PatternId("pat-1")),
                campaignRiskScore = SCORE,
                campaignRiskBand = RiskBand.HIGH,
                explanation = "scenario hit",
            )
        c.toEntity().toDomain() shouldBe c
    }

    private companion object {
        const val SCORE: Int = 80
        const val SIXTY: Long = 60
    }
}
