package com.qalqan.antifraud.export

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

class CsvFormatterTest {
    private val number =
        ExportRecord.SuspiciousNumber(
            phoneFull = "+77001234567",
            phoneLast4 = "4567",
            isShortCode = false,
            displayName = "Bank Alfa",
            trustStatus = "suspicious",
            firstSeenAt = Instant.parse("2026-05-01T10:00:00Z"),
            riskCounter = 3,
        )
    private val request = ExportRequest(setOf(ExportCategory.SUSPICIOUS_NUMBERS), ExportFormat.CSV)

    @Test
    fun `RFC 4180 line endings are CRLF`() {
        val text = CsvFormatter.format(listOf(number), request).toString(Charsets.UTF_8)
        text.contains("\r\n") shouldBe true
        text.contains("\n\r") shouldBe false
    }

    @Suppress("MaxLineLength")
    @Test
    fun `emits one section per category with category header line, column header, then rows`() {
        val text = CsvFormatter.format(listOf(number), request).toString(Charsets.UTF_8)
        text.contains("# suspicious_numbers\r\n") shouldBe true
        text.contains("phoneFull,phoneLast4,isShortCode,displayName,trustStatus,firstSeenAt,riskCounter\r\n") shouldBe true
        text.contains("+77001234567,4567,false,Bank Alfa,suspicious,2026-05-01T10:00:00Z,3\r\n") shouldBe true
    }

    @Test
    fun `values containing commas are quoted`() {
        val odd = number.copy(displayName = "Bank, Alfa")
        val text = CsvFormatter.format(listOf(odd), request).toString(Charsets.UTF_8)
        text.contains("\"Bank, Alfa\"") shouldBe true
    }

    @Test
    fun `values containing double quotes are escaped via RFC 4180 quote-doubling`() {
        val odd = number.copy(displayName = "Bank \"Alfa\"")
        val text = CsvFormatter.format(listOf(odd), request).toString(Charsets.UTF_8)
        text.contains("\"Bank \"\"Alfa\"\"\"") shouldBe true
    }

    @Test
    fun `null phoneFull renders as empty field, no comma drift`() {
        val redacted = number.copy(phoneFull = null)
        val text = CsvFormatter.format(listOf(redacted), request).toString(Charsets.UTF_8)
        // First column empty: line starts with a comma immediately after column-header CRLF.
        text.contains(",4567,false,Bank Alfa,suspicious,2026-05-01T10:00:00Z,3\r\n") shouldBe true
    }

    @Test
    fun `same input produces byte-identical output`() {
        val a = CsvFormatter.format(listOf(number), request)
        val b = CsvFormatter.format(listOf(number), request)
        a.contentEquals(b) shouldBe true
    }
}
