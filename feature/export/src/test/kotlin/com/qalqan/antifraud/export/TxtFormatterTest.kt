package com.qalqan.antifraud.export

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

class TxtFormatterTest {
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

    private val request =
        ExportRequest(
            categories = setOf(ExportCategory.SUSPICIOUS_NUMBERS),
            format = ExportFormat.TXT,
        )

    @Test
    fun `emits a header section for each category present in the request`() {
        val bytes = TxtFormatter.format(listOf(number), request)
        val text = bytes.toString(Charsets.UTF_8)
        text.contains("=== SUSPICIOUS_NUMBERS ===") shouldBe true
    }

    @Test
    fun `emits one record per line with key colon value separators`() {
        val bytes = TxtFormatter.format(listOf(number), request)
        val text = bytes.toString(Charsets.UTF_8)
        text.contains("phoneFull: +77001234567") shouldBe true
        text.contains("phoneLast4: 4567") shouldBe true
        text.contains("riskCounter: 3") shouldBe true
    }

    @Test
    fun `null phoneFull is rendered as empty string (no trailing characters)`() {
        val redacted = number.copy(phoneFull = null)
        val bytes = TxtFormatter.format(listOf(redacted), request)
        val text = bytes.toString(Charsets.UTF_8)
        text.contains("phoneFull: \n") shouldBe true
    }

    @Test
    fun `output is UTF-8 with no BOM and LF line endings`() {
        val bytes = TxtFormatter.format(listOf(number), request)
        // No UTF-8 BOM (0xEF 0xBB 0xBF) at byte 0.
        (bytes.size >= 3) shouldBe true
        (bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) shouldBe false
        // LF, not CRLF.
        bytes.toString(Charsets.UTF_8).contains("\r\n") shouldBe false
    }

    @Test
    fun `same input produces byte-identical output (determinism)`() {
        val a = TxtFormatter.format(listOf(number), request)
        val b = TxtFormatter.format(listOf(number), request)
        a.contentEquals(b) shouldBe true
    }
}
