package com.qalqan.antifraud.export

/**
 * Spec §8.4 — applies a set of [AnonymizationOption]s to a list of [ExportRecord]s. The
 * three operational redactors touch orthogonal fields (phone, domain, timestamp), so
 * composition is commutative; order of application does not matter and the test suite
 * pins this invariant in T13.
 *
 * `default()` returns a pipeline wired to the three operational redactors. Tests can
 * inject their own `Map<AnonymizationOption, Redactor>` for custom coverage.
 */
class RedactionPipeline internal constructor(
    private val redactors: Map<AnonymizationOption, Redactor>,
) {
    fun apply(
        records: List<ExportRecord>,
        options: Set<AnonymizationOption>,
    ): List<ExportRecord> {
        if (options.isEmpty()) return records
        var current = records
        options.forEach { option ->
            val redactor = redactors[option] ?: error("no redactor for option $option")
            current = current.map(redactor::apply)
        }
        return current
    }

    companion object {
        fun default(): RedactionPipeline =
            RedactionPipeline(
                redactors =
                    mapOf(
                        AnonymizationOption.NumbersLast4 to NumbersLast4Redactor,
                        AnonymizationOption.DomainZoneOnly to DomainZoneOnlyRedactor,
                        AnonymizationOption.DatesDayOnly to DatesDayOnlyRedactor,
                    ),
            )
    }
}
