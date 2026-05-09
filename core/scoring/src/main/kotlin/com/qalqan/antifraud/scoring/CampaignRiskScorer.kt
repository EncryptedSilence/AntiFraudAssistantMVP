package com.qalqan.antifraud.scoring

import com.qalqan.antifraud.domain.EventId
import java.time.Duration
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Spec §11.4:
 * CampaignRiskScore = round(min(100, Σ_i (EventRisk_i * TimeDecay_i * LinkStrength_i) + PatternRisk)).
 *
 * UserAnswerRisk is NOT added separately at the campaign level — it is already included via EventRisk_i.
 * v1 added it twice; v2 removes the duplicate.
 */
object CampaignRiskScorer {
    data class Contribution(
        val eventId: EventId,
        val eventRisk: Int,
        val age: Duration,
        val signals: Set<LinkSignal>,
    ) {
        init {
            require(eventRisk in 0..100) { "eventRisk must be in 0..100" }
        }
    }

    fun compute(
        contributions: List<Contribution>,
        triggeredPatternWeights: List<Int>,
    ): Int {
        val eventSum =
            contributions.sumOf { c ->
                val decay = TimeDecayTable.coefficient(c.age)
                val link = LinkStrengthTable.combine(c.signals)
                c.eventRisk * decay * link
            }
        val total = eventSum + PatternRisk.compute(triggeredPatternWeights)
        return min(total.roundToInt(), 100).coerceAtLeast(0)
    }
}
