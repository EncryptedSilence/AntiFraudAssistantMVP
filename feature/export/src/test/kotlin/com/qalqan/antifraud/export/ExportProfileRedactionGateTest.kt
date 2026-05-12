package com.qalqan.antifraud.export

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ExportProfileRedactionGateTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `write returns PreviewNotShown for an unknown token (§16_10 redaction gate)`() {
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
                    )
                val r = orchestrator.write(req, repos, android.net.Uri.fromFile(file), previewToken = "bogus")
                r.isFailure shouldBe true
                (r.exceptionOrNull() === ExportWriteError.PreviewNotShown) shouldBe true
                repos.exportProfiles.count() shouldBe 0
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun `write returns PreviewNotShown when the request shape changed since preview (§16_10)`() {
        val previewReq = ExportRequest(setOf(ExportCategory.SUSPICIOUS_NUMBERS), ExportFormat.JSON)
        val mutatedReq = previewReq.copy(format = ExportFormat.CSV)
        val file = File.createTempFile("export", ".tmp", context.cacheDir)
        try {
            runBlocking {
                val orchestrator =
                    ExportOrchestrator(
                        gatherer = ExportGatherer.default(context),
                        pipeline = RedactionPipeline.default(),
                        contentResolver = context.contentResolver,
                        clock = { java.time.Instant.parse("2026-05-12T10:00:00Z") },
                    )
                val preview = orchestrator.preview(previewReq, repos).getOrThrow()
                val r = orchestrator.write(mutatedReq, repos, android.net.Uri.fromFile(file), preview.token)
                r.isFailure shouldBe true
                (r.exceptionOrNull() === ExportWriteError.PreviewNotShown) shouldBe true
            }
        } finally {
            file.delete()
        }
    }
}
