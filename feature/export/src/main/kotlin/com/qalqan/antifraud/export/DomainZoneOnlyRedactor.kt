package com.qalqan.antifraud.export

/**
 * Spec §8.4 — "keep only the eTLD zone". For Stage 7 the operational record variants
 * ([ExportRecord.SuspiciousNumber], [ExportRecord.RiskCampaign], [ExportRecord.TriggeredPattern])
 * do not carry a domain field, so this redactor is structurally a no-op on every Stage 7
 * record. The helper [extractZone] is exposed so the test suite + future record variants
 * can share the implementation; Stage 8 / 9 will wire it through when a `SuspiciousDomain`
 * or `RiskCampaign.domain` projection lands.
 *
 * "Zone" is the last dot-segment of the input. For domains that already came through
 * `:feature:web`'s `DomainNormalizer`, the input is eTLD+1 (e.g. `example.kz`), so the
 * last segment is the eTLD by construction.
 */
internal object DomainZoneOnlyRedactor : Redactor {
    override fun apply(record: ExportRecord): ExportRecord = record

    fun extractZone(domain: String): String {
        val lastDot = domain.lastIndexOf('.')
        return if (lastDot < 0) "*.$domain" else "*.${domain.substring(lastDot + 1)}"
    }
}
