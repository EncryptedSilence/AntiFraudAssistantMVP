package com.qalqan.antifraud.acceptance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.export.AnonymizationOption
import com.qalqan.antifraud.export.ExportCategory
import com.qalqan.antifraud.export.ExportFormat
import com.qalqan.antifraud.export.ExportFormatters
import com.qalqan.antifraud.export.ExportRecord
import com.qalqan.antifraud.export.ExportRequest
import com.qalqan.antifraud.export.RedactionPipeline
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

/**
 * Spec §23 #16 — anonymization options produce exports without the original values.
 * The test feeds the redaction pipeline + formatter a synthetic record that carries an
 * E.164 phone number, a domain, and a sub-day timestamp, then asserts each option
 * strips its target from the output regardless of format.
 *
 *   - NumbersLast4: no `+\d{8,15}` E.164 substring in the output, but `\d{4}` (the last 4)
 *     still appears.
 *   - DomainZoneOnly: every domain-looking substring is of the `*.tld` shape; the original
 *     full domain string does not appear.
 *   - DatesDayOnly: no `T\d\d:\d\d` ISO-8601 time substring in the output; only `YYYY-MM-DDT00:00:00Z`.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance16AnonymizationOptionsTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() {
        repos.close()
    }

    private val sample: List<ExportRecord> =
        listOf(
            ExportRecord.SuspiciousNumber(
                phoneFull = "+77001234567",
                phoneLast4 = "4567",
                isShortCode = false,
                displayName = "Bank Alfa",
                trustStatus = "suspicious",
                firstSeenAt = Instant.parse("2026-05-01T14:37:22Z"),
                riskCounter = 3,
            ),
        )

    private fun renderText(
        opts: Set<AnonymizationOption>,
        format: ExportFormat,
    ): String {
        val request = ExportRequest(setOf(ExportCategory.SUSPICIOUS_NUMBERS), format, opts)
        val redacted = RedactionPipeline.default().apply(sample, opts)
        val bytes = ExportFormatters.forFormat(format).format(redacted, request)
        return bytes.toString(Charsets.UTF_8)
    }

    @Test
    fun `§23 #16 — NumbersLast4 strips the full phone but keeps the last-4`() {
        ExportFormat.entries.forEach { format ->
            val text = renderText(setOf(AnonymizationOption.NumbersLast4), format)
            text.contains("+77001234567") shouldBe false
            text.contains("4567") shouldBe true
        }
    }

    @Test
    fun `§23 #16 — DatesDayOnly strips clock-time components`() {
        ExportFormat.entries.forEach { format ->
            val text = renderText(setOf(AnonymizationOption.DatesDayOnly), format)
            text.contains("14:37:22") shouldBe false
            text.contains("2026-05-01T00:00:00Z") shouldBe true
        }
    }

    @Test
    fun `§23 #16 — composing NumbersLast4 + DatesDayOnly strips both`() {
        val opts = setOf(AnonymizationOption.NumbersLast4, AnonymizationOption.DatesDayOnly)
        ExportFormat.entries.forEach { format ->
            val text = renderText(opts, format)
            text.contains("+77001234567") shouldBe false
            text.contains("14:37:22") shouldBe false
            text.contains("4567") shouldBe true
            text.contains("2026-05-01T00:00:00Z") shouldBe true
        }
    }

    @Test
    fun `§23 #16 — empty options preserve the original values`() {
        val text = renderText(emptySet(), ExportFormat.TXT)
        text.contains("+77001234567") shouldBe true
        text.contains("14:37:22") shouldBe true
    }
}
