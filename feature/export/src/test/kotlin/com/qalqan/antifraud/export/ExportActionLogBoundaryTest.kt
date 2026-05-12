package com.qalqan.antifraud.export

import android.content.ContentResolver
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class ExportActionLogBoundaryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `successful export emits export_preview_shown and export_completed markers, never URI or PII`() {
        val req = ExportRequest(setOf(ExportCategory.SUSPICIOUS_NUMBERS), ExportFormat.JSON)
        val file = File.createTempFile("export", ".tmp", context.cacheDir)
        try {
            runBlocking {
                val orchestrator =
                    ExportOrchestrator(
                        gatherer = ExportGatherer.default(context),
                        pipeline = RedactionPipeline.default(),
                        contentResolver = context.contentResolver,
                        clock = { java.time.Instant.parse("2026-05-12T10:00:00Z") },
                        actionLogger = repos.actionLogger,
                    )
                val preview = orchestrator.preview(req, repos).getOrThrow()
                orchestrator.write(req, repos, android.net.Uri.fromFile(file), preview.token).isSuccess shouldBe true

                val entries = repos.actionLog.recent(limit = 50)
                val exportEntries =
                    entries.filter { it.action == AppAction.SETTING_CHANGED }
                        .filter { it.details["setting"]?.startsWith("export_") == true }
                exportEntries.map { it.details["setting"] } shouldBe listOf("export_completed", "export_preview_shown")

                // Forbidden keys per §20.1 / §2.1 — extend the Stage 5 / 6 set to cover SAF Uri strings.
                val forbiddenKeys =
                    setOf(
                        "phone", "domain", "url", "uri", "path", "name", "comment", "body",
                        "exception", "message", "cause", "stacktrace",
                    )
                exportEntries.forEach { entry ->
                    entry.details.keys.intersect(forbiddenKeys) shouldBe emptySet()
                    entry.details.values.forEach { value ->
                        value.contains(file.absolutePath) shouldBe false
                        value.contains("file://") shouldBe false
                    }
                }
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun `failed write emits export_failed marker with step label only`() {
        val req = ExportRequest(setOf(ExportCategory.SUSPICIOUS_NUMBERS), ExportFormat.JSON)
        val bogus = android.net.Uri.parse("content://com.example.nonexistent.provider/file")
        val mockResolver = mockk<ContentResolver>()
        every { mockResolver.openOutputStream(bogus) } throws IOException("provider not found")
        runBlocking {
            val orchestrator =
                ExportOrchestrator(
                    gatherer = ExportGatherer.default(context),
                    pipeline = RedactionPipeline.default(),
                    contentResolver = mockResolver,
                    clock = { java.time.Instant.parse("2026-05-12T10:00:00Z") },
                    actionLogger = repos.actionLogger,
                )
            val preview = orchestrator.preview(req, repos).getOrThrow()
            orchestrator.write(req, repos, bogus, preview.token).isFailure shouldBe true

            val entries = repos.actionLog.recent(limit = 50)
            val failedEntries =
                entries.filter { it.action == AppAction.SETTING_CHANGED }
                    .filter { it.details["setting"] == "export_failed" }
            (failedEntries.isNotEmpty()) shouldBe true
            failedEntries.first().details["step"] shouldBe "write"
            // No URI / path / exception detail leaks.
            failedEntries.first().details["uri"] shouldBe null
            failedEntries.first().details["exception"] shouldBe null
        }
    }
}
