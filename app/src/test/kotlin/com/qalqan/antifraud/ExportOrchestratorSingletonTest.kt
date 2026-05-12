package com.qalqan.antifraud

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
import java.io.File
import java.time.Instant

/**
 * Regression guard for the T32 bug: preview and write must share the same
 * ExportOrchestrator instance so the in-memory pendingPreviews map is visible
 * to the write call. This mirrors StatusViewModel's exported lazy field contract.
 */
@RunWith(RobolectricTestRunner::class)
class ExportOrchestratorSingletonTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() {
        repos.close()
    }

    private fun sharedOrchestrator(): ExportOrchestrator =
        ExportOrchestrator(
            gatherer = ExportGatherer.default(context),
            pipeline = RedactionPipeline.default(),
            contentResolver = context.contentResolver,
            clock = { Instant.now() },
        )

    @Test
    fun `preview token from generateExportPreview is visible to writeExport via shared orchestrator`() {
        val req = ExportRequest(setOf(ExportCategory.SUSPICIOUS_NUMBERS), ExportFormat.JSON)
        val file = File.createTempFile("export-singleton", ".tmp", context.cacheDir)
        try {
            runBlocking {
                val orchestrator = sharedOrchestrator()
                val preview = orchestrator.preview(req, repos).getOrThrow()
                val r = orchestrator.write(req, repos, android.net.Uri.fromFile(file), preview.token)
                r.isSuccess shouldBe true
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun `two separate orchestrator instances cannot share a preview token`() {
        val req = ExportRequest(setOf(ExportCategory.SUSPICIOUS_NUMBERS), ExportFormat.JSON)
        val file = File.createTempFile("export-two-instances", ".tmp", context.cacheDir)
        try {
            runBlocking {
                val orchestratorA = sharedOrchestrator()
                val orchestratorB = sharedOrchestrator()
                val preview = orchestratorA.preview(req, repos).getOrThrow()
                val r = orchestratorB.write(req, repos, android.net.Uri.fromFile(file), preview.token)
                r.isFailure shouldBe true
            }
        } finally {
            file.delete()
        }
    }
}
