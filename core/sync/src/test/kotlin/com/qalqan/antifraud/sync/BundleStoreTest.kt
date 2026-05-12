package com.qalqan.antifraud.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.crypto.BundleManifest
import com.qalqan.antifraud.crypto.BundlePriority
import com.qalqan.antifraud.crypto.VerifiedBundle
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class BundleStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun verified(
        version: String = "v1",
        patterns: ByteArray = "patterns-v1".toByteArray(),
    ): Pair<ByteArray, VerifiedBundle> {
        val manifest = BundleManifest(
            version = version,
            createdAt = Instant.parse("2026-05-12T10:00:00Z"),
            source = "stable",
            schemaVersion = 1,
            minAppVersion = 1,
            priority = BundlePriority.NORMAL,
            previousPackageId = null,
            contents = mapOf("data/patterns.json" to "sha256:${"a".repeat(64)}"),
        )
        val raw = "raw-bundle-$version".toByteArray()
        return raw to VerifiedBundle(manifest, mapOf("data/patterns.json" to patterns))
    }

    @Test
    fun `activate writes raw bundle and data entries under filesDir-sync-current`() {
        val store = BundleStore(context)
        val (raw, v) = verified()
        val r = store.activate(raw, v)
        r.isSuccess shouldBe true

        val current = File(context.filesDir, "sync/current")
        File(current, "bundle.afpkg").readBytes().contentEquals(raw) shouldBe true
        File(current, "data/patterns.json").readBytes().contentEquals(
            v.dataEntries.getValue("data/patterns.json"),
        ) shouldBe true

        File(context.filesDir, "sync/previous").exists() shouldBe false
    }
}
