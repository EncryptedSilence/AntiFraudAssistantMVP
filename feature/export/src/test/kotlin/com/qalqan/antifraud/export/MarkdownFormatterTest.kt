package com.qalqan.antifraud.export

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

class MarkdownFormatterTest {
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
    private val request = ExportRequest(setOf(ExportCategory.SUSPICIOUS_NUMBERS), ExportFormat.MARKDOWN)

    @Test
    fun `emits an H2 heading per category`() {
        val text = MarkdownFormatter.format(listOf(number), request).toString(Charsets.UTF_8)
        text.contains("## SUSPICIOUS_NUMBERS") shouldBe true
    }

    @Suppress("MaxLineLength")
    @Test
    fun `emits a Markdown table with the right columns for SuspiciousNumber`() {
        val text = MarkdownFormatter.format(listOf(number), request).toString(Charsets.UTF_8)
        text.contains("| phoneFull | phoneLast4 | isShortCode | displayName | trustStatus | firstSeenAt | riskCounter |") shouldBe true
        text.contains("| --- | --- | --- | --- | --- | --- | --- |") shouldBe true
        text.contains("| +77001234567 | 4567 | false | Bank Alfa | suspicious | 2026-05-01T10:00:00Z | 3 |") shouldBe true
    }

    @Test
    fun `pipe characters in field values are escaped to keep the Markdown table well-formed`() {
        val pipeName = number.copy(displayName = "Foo | Bar")
        val text = MarkdownFormatter.format(listOf(pipeName), request).toString(Charsets.UTF_8)
        text.contains("Foo \\| Bar") shouldBe true
    }

    @Test
    fun `same input produces byte-identical output`() {
        val a = MarkdownFormatter.format(listOf(number), request)
        val b = MarkdownFormatter.format(listOf(number), request)
        a.contentEquals(b) shouldBe true
    }
}
