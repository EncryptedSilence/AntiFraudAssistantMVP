package com.qalqan.antifraud.export

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

class JsonFormatterTest {
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
    private val request = ExportRequest(setOf(ExportCategory.SUSPICIOUS_NUMBERS), ExportFormat.JSON)

    @Test
    fun `emits a single root object with a key per category`() {
        val text = JsonFormatter.format(listOf(number), request).toString(Charsets.UTF_8)
        text.startsWith("{") shouldBe true
        text.contains("\"suspicious_numbers\":") shouldBe true
    }

    @Test
    fun `emits an array of records for each category`() {
        val text = JsonFormatter.format(listOf(number), request).toString(Charsets.UTF_8)
        text.contains("\"phoneFull\":\"+77001234567\"") shouldBe true
        text.contains("\"phoneLast4\":\"4567\"") shouldBe true
        text.contains("\"riskCounter\":3") shouldBe true
    }

    @Test
    fun `null phoneFull is emitted as explicit null`() {
        val redacted = number.copy(phoneFull = null)
        val text = JsonFormatter.format(listOf(redacted), request).toString(Charsets.UTF_8)
        text.contains("\"phoneFull\":null") shouldBe true
    }

    @Test
    fun `Instant is serialized as ISO-8601 UTC string`() {
        val text = JsonFormatter.format(listOf(number), request).toString(Charsets.UTF_8)
        text.contains("\"firstSeenAt\":\"2026-05-01T10:00:00Z\"") shouldBe true
    }

    @Test
    fun `same input produces byte-identical output`() {
        val a = JsonFormatter.format(listOf(number), request)
        val b = JsonFormatter.format(listOf(number), request)
        a.contentEquals(b) shouldBe true
    }

    @Suppress("MaxLineLength")
    @Test
    fun `empty category emits an empty array (not omitted)`() {
        val req = ExportRequest(setOf(ExportCategory.SUSPICIOUS_NUMBERS, ExportCategory.RISK_CAMPAIGNS), ExportFormat.JSON)
        val text = JsonFormatter.format(listOf(number), req).toString(Charsets.UTF_8)
        text.contains("\"risk_campaigns\":[]") shouldBe true
    }
}
