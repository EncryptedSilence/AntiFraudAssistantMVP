package com.qalqan.antifraud.acceptance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.crypto.BundleArchiveReader
import com.qalqan.antifraud.crypto.BundleVerifier
import com.qalqan.antifraud.crypto.Ed25519SignatureVerifier
import com.qalqan.antifraud.sync.BundleStore
import com.qalqan.antifraud.sync.SyncDownloader
import com.qalqan.antifraud.sync.SyncOrchestrator
import com.qalqan.antifraud.sync.SyncOutcome
import com.qalqan.antifraud.sync.SyncSettings
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §23 #4 — a default install never makes an outbound request. Stage 6 holds this
 * by construction: `SyncSettings.enabled` defaults to `false`, and the orchestrator
 * short-circuits to [SyncOutcome.Disabled] before invoking the downloader.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance4ZeroEgressDefaultTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `§23 #4 — fresh install never invokes downloader with default settings`() {
        val settings = SyncSettings(context)
        settings.enabled shouldBe false

        var callCount = 0
        val throwingDownloader =
            object : SyncDownloader {
                override suspend fun fetchLatest(url: String): Result<ByteArray> {
                    callCount += 1
                    error("downloader must NOT be called when sync is disabled")
                }
            }
        val orchestrator =
            SyncOrchestrator(
                settings = settings,
                downloader = throwingDownloader,
                archiveReader = BundleArchiveReader(),
                verifier = BundleVerifier(Ed25519SignatureVerifier(), ByteArray(32)),
                store = BundleStore(context),
                actionLogger = null,
            )

        runBlocking {
            val outcome = orchestrator.runOnce("https://example.invalid/")
            outcome shouldBe SyncOutcome.Disabled
        }
        callCount shouldBe 0
    }
}
