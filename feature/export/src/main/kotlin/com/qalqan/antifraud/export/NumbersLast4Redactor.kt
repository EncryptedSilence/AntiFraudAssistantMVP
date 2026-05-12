package com.qalqan.antifraud.export

/**
 * Spec §8.4 — "keep only the last 4 digits". Replaces [ExportRecord.SuspiciousNumber.phoneFull]
 * with `null`; [phoneLast4] is already the last-4 fallback (§16.1 ContactProfile.phoneLast4)
 * so no transformation of the trailing digits is needed.
 *
 * Non-`SuspiciousNumber` records pass through unchanged.
 */
internal object NumbersLast4Redactor : Redactor {
    override fun apply(record: ExportRecord): ExportRecord =
        when (record) {
            is ExportRecord.SuspiciousNumber -> record.copy(phoneFull = null)
            else -> record
        }
}
