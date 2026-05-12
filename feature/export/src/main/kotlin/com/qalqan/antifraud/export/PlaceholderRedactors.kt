package com.qalqan.antifraud.export

internal object NumbersLast4Redactor : Redactor {
    override fun apply(record: ExportRecord): ExportRecord = record
}

internal object DomainZoneOnlyRedactor : Redactor {
    override fun apply(record: ExportRecord): ExportRecord = record
}

internal object DatesDayOnlyRedactor : Redactor {
    override fun apply(record: ExportRecord): ExportRecord = record
}
