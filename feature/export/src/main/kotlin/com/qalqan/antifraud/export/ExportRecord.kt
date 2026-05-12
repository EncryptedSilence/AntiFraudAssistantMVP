package com.qalqan.antifraud.export

import java.time.Instant

/**
 * Spec §8 — the in-memory form every formatter consumes. One sealed interface variant per
 * [ExportCategory]; each variant carries the minimum fields needed for all four formats
 * (TXT / Markdown / JSON / CSV). The redaction pipeline (Phase 3) mutates these into
 * partially-cleared variants without changing the type — e.g. `NumbersLast4` clears
 * `phoneFull` and `firstSeenAt` is replaced with a date-truncated [Instant] by
 * `DatesDayOnly`.
 *
 * `category` is a property (not a constructor field) so each variant can return its
 * fixed category without a per-instance allocation.
 */
sealed interface ExportRecord {
    val category: ExportCategory

    /** Spec §16.1 ContactProfile — exported as a "suspicious numbers" row. */
    data class SuspiciousNumber(
        val phoneFull: String?,
        val phoneLast4: String,
        val isShortCode: Boolean,
        val displayName: String?,
        val trustStatus: String,
        val firstSeenAt: Instant,
        val riskCounter: Int,
    ) : ExportRecord {
        override val category: ExportCategory = ExportCategory.SUSPICIOUS_NUMBERS
    }

    /** Spec §16.7 RiskCampaign — exported as a single row (campaign-level summary). */
    data class RiskCampaign(
        val campaignId: String,
        val startedAt: Instant,
        val lastEventAt: Instant,
        val status: String,
        val scenarioType: String,
        val campaignRiskScore: Int,
        val campaignRiskLevel: String,
        val relatedEventCount: Int,
        val explanation: String,
    ) : ExportRecord {
        override val category: ExportCategory = ExportCategory.RISK_CAMPAIGNS
    }

    /** Spec §16.8 ScenarioPattern — exported as a triggered-pattern row. */
    data class TriggeredPattern(
        val patternId: String,
        val name: String,
        val scenarioCategory: String,
        val version: String,
        val triggeredAt: Instant,
        val timesTriggered: Int,
    ) : ExportRecord {
        override val category: ExportCategory = ExportCategory.TRIGGERED_PATTERNS
    }
}
