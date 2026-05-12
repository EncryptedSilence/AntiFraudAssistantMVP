package com.qalqan.antifraud.export

internal object TxtFormatter : ExportFormatter {
    override val format = ExportFormat.TXT

    override fun format(
        records: List<ExportRecord>,
        request: ExportRequest,
    ): ByteArray = ByteArray(0)
}

internal object MarkdownFormatter : ExportFormatter {
    override val format = ExportFormat.MARKDOWN

    override fun format(
        records: List<ExportRecord>,
        request: ExportRequest,
    ): ByteArray = ByteArray(0)
}

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
