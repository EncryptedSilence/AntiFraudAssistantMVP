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
import com.qalqan.antifraud.domain.AppAction
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
class LocalBundleImporterTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() { repos.close() }

    private fun buildAfpkg(source: String = "stable"): ByteArray {
        val patterns = "patterns-payload".toByteArray()
        val manifest = BundleManifest(
            version = "2026.05.12-001",
            createdAt = Instant.parse("2026-05-12T10:00:00Z"),
            source = source,
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

    private fun newImporter(): LocalBundleImporter = LocalBundleImporter(
        archiveReader = BundleArchiveReader(),
        verifier = BundleVerifier(
            Ed25519SignatureVerifier(),
            TestKeys.hexToBytes(TestKeys.TEST_PUBLIC_KEY_HEX),
        ),
        store = BundleStore(context),
        actionLogger = repos.actionLogger,
    )

    @Test
    fun `valid fixture stream resolves to Activated and writes local_bundle_imported marker`() {
        runBlocking {
            val outcome = newImporter().import(ByteArrayInputStream(buildAfpkg(source = "stable")))
            outcome.shouldBeInstanceOf<SyncOutcome.Activated>()
        }
        val entries = runBlocking { repos.actionLog.recent(limit = 50) }
        val marker = entries.filter { it.action == AppAction.SETTING_CHANGED }
            .filter { it.details["setting"] == "local_bundle_imported" }
        (marker.isNotEmpty()) shouldBe true
    }

    @Test
    fun `manifest source is ignored — imported bundle is tagged local per §7_5`() {
        runBlocking {
            val outcome = newImporter().import(ByteArrayInputStream(buildAfpkg(source = "stable")))
            outcome.shouldBeInstanceOf<SyncOutcome.Activated>()
        }
        val entries = runBlocking { repos.actionLog.recent(limit = 50) }
        val marker = entries.first {
            it.action == AppAction.SETTING_CHANGED && it.details["setting"] == "local_bundle_imported"
        }
        marker.details["channel"] shouldBe "local"
    }

    @Test
    fun `malformed stream resolves to Failed`() {
        runBlocking {
            val outcome = newImporter().import(ByteArrayInputStream(ByteArray(1024) { (it * 17).toByte() }))
            outcome.shouldBeInstanceOf<SyncOutcome.Failed>()
        }
    }
}
