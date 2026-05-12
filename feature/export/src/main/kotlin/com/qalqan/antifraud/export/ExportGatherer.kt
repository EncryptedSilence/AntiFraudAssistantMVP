package com.qalqan.antifraud.export

import android.content.Context
import com.qalqan.antifraud.database.Repositories

/**
 * Spec §8 — turns an [ExportRequest] into the raw record list, drawing from existing
 * `Repositories`. Each category has its own gather arm; the orchestrator union-merges
 * the per-arm results in [gather].
 *
 * Gathering is read-only — no inserts, no updates. The full-fidelity records flow into
 * the [RedactionPipeline] (Phase 3) before any formatter sees them.
 */
class ExportGatherer internal constructor(
    private val arms: Map<ExportCategory, GathererArm>,
) {
    suspend fun gather(
        request: ExportRequest,
        repositories: Repositories,
    ): List<ExportRecord> {
        val out = mutableListOf<ExportRecord>()
        request.categories.forEach { category ->
            val arm = arms[category] ?: error("no gatherer arm for category $category")
            out += arm.gather(repositories)
        }
        return out
    }

    companion object {
        /**
         * The Stage 7 wiring. `context` is captured for the [TriggeredPatternsGathererArm],
         * which needs to read the in-APK seed catalog via `SeedPatternLoader`.
         */
        fun default(context: Context): ExportGatherer =
            ExportGatherer(
                arms =
                    mapOf(
                        ExportCategory.SUSPICIOUS_NUMBERS to SuspiciousNumbersGathererArm,
                        ExportCategory.RISK_CAMPAIGNS to RiskCampaignsGathererArm,
                        ExportCategory.TRIGGERED_PATTERNS to TriggeredPatternsGathererArm(context),
                    ),
            )
    }
}

/** Spec §8 — one arm per [ExportCategory]; converts repository rows into [ExportRecord]s. */
internal interface GathererArm {
    suspend fun gather(repositories: Repositories): List<ExportRecord>
}
