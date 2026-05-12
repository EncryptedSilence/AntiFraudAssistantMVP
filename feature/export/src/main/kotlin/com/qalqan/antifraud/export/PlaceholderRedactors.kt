package com.qalqan.antifraud.export

internal object DomainZoneOnlyRedactor : Redactor {
    override fun apply(record: ExportRecord): ExportRecord = record
}

internal object DatesDayOnlyRedactor : Redactor {
    override fun apply(record: ExportRecord): ExportRecord = record
}
