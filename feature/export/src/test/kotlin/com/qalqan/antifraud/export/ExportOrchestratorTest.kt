package com.qalqan.antifraud.export

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ExportOrchestratorTest {
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
            clock = { java.time.Instant.parse("2026-05-12T10:00:00Z") },
            idGenerator = { "e-fixed" },
        )

    @Test
    fun `preview returns the bytes the formatter would emit`() {
        val req = ExportRequest(setOf(ExportCategory.SUSPICIOUS_NUMBERS), ExportFormat.JSON)
        runBlocking {
            val preview = newOrchestrator().preview(req, repos)
            preview.isSuccess shouldBe true
            preview.getOrThrow().bytes.size shouldBe
                ExportFormatters.forFormat(ExportFormat.JSON)
                    .format(emptyList(), req).size
        }
    }

    @Test
    fun `write without a prior preview returns Failure with PreviewNotShown`() {
        val req = ExportRequest(setOf(ExportCategory.SUSPICIOUS_NUMBERS), ExportFormat.JSON)
        val file = File.createTempFile("export", ".tmp", context.cacheDir)
        try {
            runBlocking {
                val result =
                    newOrchestrator().write(
                        request = req,
                        repositories = repos,
                        uri = android.net.Uri.fromFile(file),
                        previewToken = "no-such-token",
                    )
                result.isFailure shouldBe true
                val err = result.exceptionOrNull()
                err.shouldBeInstanceOf<ExportWriteError.PreviewNotShown>()
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun `write with a confirmed preview token persists ExportProfile row and writes file`() {
        val req = ExportRequest(setOf(ExportCategory.SUSPICIOUS_NUMBERS), ExportFormat.JSON)
        val file = File.createTempFile("export", ".tmp", context.cacheDir)
        try {
            runBlocking {
                val orchestrator = newOrchestrator()
                val preview = orchestrator.preview(req, repos).getOrThrow()
                val result =
                    orchestrator.write(
                        request = req,
                        repositories = repos,
                        uri = android.net.Uri.fromFile(file),
                        previewToken = preview.token,
                    )
                result.isSuccess shouldBe true
                file.readBytes().contentEquals(preview.bytes) shouldBe true

                val profile = repos.exportProfiles.findById("e-fixed")
                profile shouldBe
                    com.qalqan.antifraud.database.export.ExportProfileEntity(
                        exportId = "e-fixed",
                        createdAt = java.time.Instant.parse("2026-05-12T10:00:00Z").toEpochMilli(),
                        exportType = "suspicious_numbers",
                        includedCategories = "suspicious_numbers",
                        anonymizationLevel = "",
                        format = "JSON",
                        userConfirmed = true,
                        redactionPreviewShown = true,
                    )
            }
        } finally {
            file.delete()
        }
    }
}
