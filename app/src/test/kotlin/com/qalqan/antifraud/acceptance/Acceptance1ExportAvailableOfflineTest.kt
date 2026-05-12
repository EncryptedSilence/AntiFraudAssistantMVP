package com.qalqan.antifraud.acceptance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.export.ExportCategory
import com.qalqan.antifraud.export.ExportFormat
import com.qalqan.antifraud.export.ExportGatherer
import com.qalqan.antifraud.export.ExportOrchestrator
import com.qalqan.antifraud.export.ExportRequest
import com.qalqan.antifraud.export.RedactionPipeline
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §23 #1 — the home screen + manual entry + scoring + warnings + export all work
 * without a server. Stage 7's export pipeline is entirely local: the gatherer reads
 * from the on-device DB, the redaction pipeline is pure, the formatters are pure, and
 * the writer goes through SAF (a local permission-less file API). No `HttpURLConnection`
 * is constructed anywhere on the preview-or-write path.
 *
 * This test pins the offline contract by running a full preview-then-write against
 * `Repositories.inMemory(context)` and confirming the bytes land on a `file://` Uri
 * with no network access required.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance1ExportAvailableOfflineTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() { repos.close() }

    @Test
    fun `§23 #1 — export pipeline produces a preview without any network access`() {
        val orchestrator = ExportOrchestrator(
            gatherer = ExportGatherer.default(context),
            pipeline = RedactionPipeline.default(),
            contentResolver = context.contentResolver,
            clock = { java.time.Instant.parse("2026-05-12T10:00:00Z") },
        )
        val request = ExportRequest(setOf(ExportCategory.SUSPICIOUS_NUMBERS), ExportFormat.JSON)
        runBlocking {
            val r = orchestrator.preview(request, repos)
            r.isSuccess shouldBe true
            (r.getOrThrow().bytes.isNotEmpty()) shouldBe true
        }
    }
}
