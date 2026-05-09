package com.qalqan.antifraud.acceptance

import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.scoring.CampaignRiskScorer
import com.qalqan.antifraud.scoring.LinkSignal
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import org.junit.jupiter.api.Test
import java.time.Duration

class TimeDecayAcceptanceTest {
    @Test
    fun `7-day-old event contributes less than or equal to 60 percent of its base risk (spec §23 #11)`() {
        val baseRisk = 100
        val contribution =
            CampaignRiskScorer.Contribution(
                eventId = EventId("e"),
                eventRisk = baseRisk,
                age = Duration.ofDays(7),
                signals = setOf(LinkSignal.SAME_NUMBER),
            )
        val score = CampaignRiskScorer.compute(listOf(contribution), emptyList())
        score shouldBeLessThanOrEqual (baseRisk * 60 / 100)
    }
}
