package com.qalqan.antifraud.export

internal object DatesDayOnlyRedactor : Redactor {
    override fun apply(record: ExportRecord): ExportRecord = record
}
