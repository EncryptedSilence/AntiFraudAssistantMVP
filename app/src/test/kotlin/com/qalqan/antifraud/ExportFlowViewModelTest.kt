package com.qalqan.antifraud

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.export.AnonymizationOption
import com.qalqan.antifraud.export.ExportCategory
import com.qalqan.antifraud.export.ExportFormat
import com.qalqan.antifraud.export.ExportGatherer
import com.qalqan.antifraud.export.ExportOrchestrator
import com.qalqan.antifraud.export.ExportRequest
import com.qalqan.antifraud.export.ExportWriteError
import com.qalqan.antifraud.export.RedactionPipeline
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class ExportFlowViewModelTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() {
        repos.close()
    }

    private fun newOrchestrator(): ExportOrchestrator =
        ExportOrchestrator(
            gatherer = ExportGatherer.default(context),
            pipeline = RedactionPipeline.default(),
            contentResolver = context.contentResolver,
            clock = { Instant.parse("2026-05-12T10:00:00Z") },
        )

    @Test
    fun `generateExportPreview then writeExport lands the same bytes on disk`() {
        val req =
            ExportRequest(
                categories = setOf(ExportCategory.SUSPICIOUS_NUMBERS),
                format = ExportFormat.JSON,
                anonymization = setOf(AnonymizationOption.NumbersLast4),
            )
        val file = File.createTempFile("export", ".tmp", context.cacheDir)
        try {
            runBlocking {
                val orchestrator = newOrchestrator()
                val preview = orchestrator.preview(req, repos).getOrThrow()
                val r = orchestrator.write(req, repos, android.net.Uri.fromFile(file), preview.token)
                r.isSuccess shouldBe true
                file.readBytes().contentEquals(preview.bytes) shouldBe true
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun `stale token returns PreviewNotShown failure`() {
        val req =
            ExportRequest(
                categories = setOf(ExportCategory.SUSPICIOUS_NUMBERS),
                format = ExportFormat.JSON,
            )
        val file = File.createTempFile("export", ".tmp", context.cacheDir)
        try {
            runBlocking {
                val orchestrator = newOrchestrator()
                val r = orchestrator.write(req, repos, android.net.Uri.fromFile(file), "bogus-token")
                r.isFailure shouldBe true
                (r.exceptionOrNull() === ExportWriteError.PreviewNotShown) shouldBe true
            }
        } finally {
            file.delete()
        }
    }
}
