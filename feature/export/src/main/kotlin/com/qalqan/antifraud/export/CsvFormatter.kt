package com.qalqan.antifraud.export

internal object CsvFormatter : ExportFormatter {
    override val format = ExportFormat.CSV

    override fun format(
        records: List<ExportRecord>,
        request: ExportRequest,
    ): ByteArray = ByteArray(0)
}
