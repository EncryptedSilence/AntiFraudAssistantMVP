package com.qalqan.antifraud.acceptance

import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.scoring.CampaignRiskScorer
import com.qalqan.antifraud.scoring.LinkSignal
import io.kotest.matchers.longs.shouldBeLessThan
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.system.measureTimeMillis

class RecomputeLatencyTest {
    @Test
    fun `1000-event campaign recomputes in under 500ms (spec §23 #10)`() {
        val contributions =
            (0 until 1_000).map { i ->
                CampaignRiskScorer.Contribution(
                    eventId = EventId("e$i"),
                    eventRisk = (i % 100),
                    age = Duration.ofHours(i % 14L * 24L),
                    signals = setOf(LinkSignal.SMS_AFTER_CALL),
                )
            }
        val ms =
            measureTimeMillis {
                CampaignRiskScorer.compute(contributions, listOf(20, 30))
            }
        ms shouldBeLessThan 500L
    }
}
