package com.qalqan.antifraud.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.crypto.BundleArchiveReader
import com.qalqan.antifraud.crypto.BundleManifest
import com.qalqan.antifraud.crypto.BundleManifestJson
import com.qalqan.antifraud.crypto.BundlePriority
import com.qalqan.antifraud.crypto.BundleVerifier
import com.qalqan.antifraud.crypto.Ed25519SignatureVerifier
import com.qalqan.antifraud.crypto.Sha256
import com.qalqan.antifraud.database.Repositories
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
class SyncOrchestratorHappyPathTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() { repos.close() }

    private fun buildSignedAfpkg(): ByteArray {
        val patterns = "patterns-payload".toByteArray()
        val manifest = BundleManifest(
            version = "2026.05.12-001",
            createdAt = Instant.parse("2026-05-12T10:00:00Z"),
            source = "stable",
            schemaVersion = 1,
            minAppVersion = 1,
            priority = BundlePriority.NORMAL,
            previousPackageId = null,
            contents = mapOf("data/patterns.json" to "sha256:${Sha256.hashHex(patterns)}"),
        )
        val canonical = BundleManifestJson.toCanonicalJson(manifest)
        val signature = TestKeys.signWithTestKey(canonical)
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { zos ->
            listOf(
                "manifest.json" to canonical,
                "signature" to signature,
                "data/patterns.json" to patterns,
            ).forEach { (name, bytes) ->
                zos.putNextEntry(ZipEntry(name))
                zos.write(bytes)
                zos.closeEntry()
            }
        }
        return bos.toByteArray()
    }

    @Test
    fun `happy path activates the bundle and logs sync_completed`() {
        val settings = SyncSettings(context).also { it.enabled = true }
        val afpkg = buildSignedAfpkg()
        val downloader = object : SyncDownloader {
            override suspend fun fetchLatest(url: String): Result<ByteArray> = Result.success(afpkg)
        }
        val publicKey = TestKeys.hexToBytes(TestKeys.TEST_PUBLIC_KEY_HEX)
        val orchestrator = SyncOrchestrator(
            settings = settings,
            downloader = downloader,
            archiveReader = BundleArchiveReader(),
            verifier = BundleVerifier(Ed25519SignatureVerifier(), publicKey),
            store = BundleStore(context),
            actionLogger = repos.actionLogger,
        )
        runBlocking {
            val outcome = orchestrator.runOnce("https://example.invalid/")
            outcome.shouldBeInstanceOf<SyncOutcome.Activated>()
            outcome.bundle.manifest.version shouldBe "2026.05.12-001"
        }
    }
}
