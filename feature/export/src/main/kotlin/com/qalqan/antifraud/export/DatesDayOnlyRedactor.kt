package com.qalqan.antifraud.export

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Spec §8.4 — "round dates to the day". Every [Instant] field on the three record
 * variants is truncated to the start of its UTC day. The formatters' ISO-8601 rendering
 * then emits `YYYY-MM-DDT00:00:00Z`, which the §23 #16 acceptance test parses to confirm
 * no sub-day component remains.
 *
 * UTC is the authoritative wall-clock for the export: all `Instant`s are stored UTC in
 * the §16 data model, and the user-facing date in the preview is the UTC date, not the
 * device locale. The decision avoids a category of locale-confusion bugs when the user
 * exports on one device and reads on another in a different timezone.
 */
internal object DatesDayOnlyRedactor : Redactor {
    override fun apply(record: ExportRecord): ExportRecord =
        when (record) {
            is ExportRecord.SuspiciousNumber -> record.copy(firstSeenAt = record.firstSeenAt.truncatedToDay())
            is ExportRecord.RiskCampaign ->
                record.copy(
                    startedAt = record.startedAt.truncatedToDay(),
                    lastEventAt = record.lastEventAt.truncatedToDay(),
                )
            is ExportRecord.TriggeredPattern -> record.copy(triggeredAt = record.triggeredAt.truncatedToDay())
        }

    private fun Instant.truncatedToDay(): Instant = truncatedTo(ChronoUnit.DAYS)
}
