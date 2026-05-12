package com.qalqan.antifraud.export

internal object JsonFormatter : ExportFormatter {
    override val format = ExportFormat.JSON

    override fun format(
        records: List<ExportRecord>,
        request: ExportRequest,
    ): ByteArray = ByteArray(0)
}

internal object CsvFormatter : ExportFormatter {
    override val format = ExportFormat.CSV

    override fun format(
        records: List<ExportRecord>,
        request: ExportRequest,
    ): ByteArray = ByteArray(0)
}
