package com.qalqan.antifraud.export

import io.kotest.matchers.shouldBe
import org.junit.Test

class ExportFormatTest {
    @Test
    fun `enum has exactly four variants per spec §8_5`() {
        ExportFormat.entries.size shouldBe 4
    }

    @Test
    fun `variant names match the spec §8_5 wire formats`() {
        ExportFormat.TXT.name shouldBe "TXT"
        ExportFormat.MARKDOWN.name shouldBe "MARKDOWN"
        ExportFormat.JSON.name shouldBe "JSON"
        ExportFormat.CSV.name shouldBe "CSV"
    }

    @Test
    fun `mimeType matches the SAF picker contract`() {
        ExportFormat.TXT.mimeType shouldBe "text/plain"
        ExportFormat.MARKDOWN.mimeType shouldBe "text/markdown"
        ExportFormat.JSON.mimeType shouldBe "application/json"
        ExportFormat.CSV.mimeType shouldBe "text/csv"
    }

    @Test
    fun `fileExtension drives the suggested SAF filename`() {
        ExportFormat.TXT.fileExtension shouldBe "txt"
        ExportFormat.MARKDOWN.fileExtension shouldBe "md"
        ExportFormat.JSON.fileExtension shouldBe "json"
        ExportFormat.CSV.fileExtension shouldBe "csv"
    }
}
