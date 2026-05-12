package com.qalqan.antifraud.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.crypto.BundleArchiveReader
import com.qalqan.antifraud.crypto.BundleVerifier
import com.qalqan.antifraud.crypto.Ed25519SignatureVerifier
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SyncActionLogBoundaryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() { repos.close() }

    @Test
    fun `sync_failed action-log entry never contains URL, host, IP, or exception detail`() {
        val settings = SyncSettings(context).also { it.enabled = true }
        val downloader = object : SyncDownloader {
            override suspend fun fetchLatest(url: String): Result<ByteArray> =
                Result.failure(SyncDownloadErrorException(SyncDownloadError.Http(500)))
        }
        val orchestrator = SyncOrchestrator(
            settings = settings,
            downloader = downloader,
            archiveReader = BundleArchiveReader(),
            verifier = BundleVerifier(Ed25519SignatureVerifier(), ByteArray(32)),
            store = BundleStore(context),
            actionLogger = repos.actionLogger,
        )
        runBlocking {
            orchestrator.runOnce("https://example.invalid/super-secret-path?api_key=hunter2")
        }
        val entries = runBlocking { repos.actionLog.recent(limit = 50) }
        val syncFailedEntries = entries
            .filter { it.action == AppAction.SETTING_CHANGED }
            .filter { it.details["setting"] == "sync_failed" }
        (syncFailedEntries.isNotEmpty()) shouldBe true

        val forbiddenKeys = setOf(
            "url", "domain", "host", "ip", "exception", "message", "cause",
            "stacktrace", "code", "body",
        )
        syncFailedEntries.forEach { entry ->
            entry.details.keys.intersect(forbiddenKeys) shouldBe emptySet()
            entry.details.values.forEach { value ->
                value.contains("example.invalid") shouldBe false
                value.contains("super-secret-path") shouldBe false
                value.contains("hunter2") shouldBe false
                value.contains("500") shouldBe false
            }
        }
    }
}
