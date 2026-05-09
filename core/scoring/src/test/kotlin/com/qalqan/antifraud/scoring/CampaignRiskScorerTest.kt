package com.qalqan.antifraud.scoring

import com.qalqan.antifraud.domain.EventId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Duration

class CampaignRiskScorerTest {
    @Test fun `single recent event with no decay or link strength sums to base`() {
        val contributions =
            listOf(
                CampaignRiskScorer.Contribution(
                    eventId = EventId("e1"),
                    eventRisk = 60,
                    age = Duration.ofHours(1),
                    signals = setOf(LinkSignal.SAME_NUMBER),
                ),
            )
        // 60 * 1.0 (decay 0..24h) * 1.0 (same number) = 60, +0 patterns
        CampaignRiskScorer.compute(contributions, emptyList()) shouldBe 60
    }

    @Test fun `time decay reduces older event contribution`() {
        // decay 0.6 (5 days), strength 1.0 (USER_CONFIRMED)
        val contributions =
            listOf(
                CampaignRiskScorer.Contribution(
                    eventId = EventId("e1"),
                    eventRisk = 50,
                    age = Duration.ofDays(5),
                    signals = setOf(LinkSignal.USER_CONFIRMED),
                ),
            )
        CampaignRiskScorer.compute(contributions, emptyList()) shouldBe 30
    }

    @Test fun `events older than 14 days do not contribute`() {
        val contributions =
            listOf(
                CampaignRiskScorer.Contribution(
                    eventId = EventId("e1"),
                    eventRisk = 80,
                    age = Duration.ofDays(20),
                    signals = setOf(LinkSignal.SAME_NUMBER),
                ),
            )
        CampaignRiskScorer.compute(contributions, emptyList()) shouldBe 0
    }

    @Test fun `pattern risk adds on top, total capped at 100`() {
        val contributions =
            listOf(
                CampaignRiskScorer.Contribution(
                    eventId = EventId("e1"),
                    eventRisk = 80,
                    age = Duration.ZERO,
                    signals = setOf(LinkSignal.SAME_NUMBER),
                ),
            )
        CampaignRiskScorer.compute(contributions, listOf(40, 40)) shouldBe 100 // 80 + 60 = 140 → 100
    }

    @Test fun `multiple events sum with decay and link strength`() {
        // 80 * 1.0 * 1.0 + 50 * 0.6 * 0.9 = 80 + 27 = 107 → cap 100
        val contributions =
            listOf(
                CampaignRiskScorer.Contribution(
                    eventId = EventId("e1"),
                    eventRisk = 80,
                    age = Duration.ofHours(1),
                    signals = setOf(LinkSignal.SAME_NUMBER),
                ),
                CampaignRiskScorer.Contribution(
                    eventId = EventId("e2"),
                    eventRisk = 50,
                    age = Duration.ofDays(5),
                    signals = setOf(LinkSignal.SMS_AFTER_CALL),
                ),
            )
        CampaignRiskScorer.compute(contributions, emptyList()) shouldBe 100
    }
}
