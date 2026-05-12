package com.qalqan.antifraud.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.crypto.BundleArchiveReader
import com.qalqan.antifraud.crypto.BundleVerifier
import com.qalqan.antifraud.crypto.Ed25519SignatureVerifier
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SyncOrchestratorDisabledTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `runOnce never invokes downloader when settings_enabled is false`() {
        val settings = SyncSettings(context).also { it.enabled = false }
        var downloaderCallCount = 0
        val downloader = object : SyncDownloader {
            override suspend fun fetchLatest(url: String): Result<ByteArray> {
                downloaderCallCount += 1
                return Result.success(ByteArray(0))
            }
        }
        val orchestrator = SyncOrchestrator(
            settings = settings,
            downloader = downloader,
            archiveReader = BundleArchiveReader(),
            verifier = BundleVerifier(Ed25519SignatureVerifier(), ByteArray(32)),
            store = BundleStore(context),
            actionLogger = null,
        )
        runBlocking {
            val outcome = orchestrator.runOnce("https://example.invalid/")
            outcome shouldBe SyncOutcome.Disabled
        }
        downloaderCallCount shouldBe 0
    }
}
