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
        val manifest =
            BundleManifest(
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

    @Test
    fun `activate twice rotates first bundle into previous slot`() {
        val store = BundleStore(context)
        val (raw1, v1) = verified(version = "v1", patterns = "patterns-v1".toByteArray())
        val (raw2, v2) = verified(version = "v2", patterns = "patterns-v2".toByteArray())

        store.activate(raw1, v1).isSuccess shouldBe true
        store.activate(raw2, v2).isSuccess shouldBe true

        val current = File(context.filesDir, "sync/current")
        val previous = File(context.filesDir, "sync/previous")

        File(current, "bundle.afpkg").readBytes().contentEquals(raw2) shouldBe true
        File(current, "data/patterns.json").readBytes().contentEquals(
            v2.dataEntries.getValue("data/patterns.json"),
        ) shouldBe true

        File(previous, "bundle.afpkg").readBytes().contentEquals(raw1) shouldBe true
        File(previous, "data/patterns.json").readBytes().contentEquals(
            v1.dataEntries.getValue("data/patterns.json"),
        ) shouldBe true
    }

    @Test
    fun `third activate overwrites the older previous (N = 1 retention)`() {
        val store = BundleStore(context)
        val (raw1, v1) = verified(version = "v1", patterns = "patterns-v1".toByteArray())
        val (raw2, v2) = verified(version = "v2", patterns = "patterns-v2".toByteArray())
        val (raw3, v3) = verified(version = "v3", patterns = "patterns-v3".toByteArray())

        store.activate(raw1, v1).isSuccess shouldBe true
        store.activate(raw2, v2).isSuccess shouldBe true
        store.activate(raw3, v3).isSuccess shouldBe true

        val previous = File(context.filesDir, "sync/previous")
        File(previous, "bundle.afpkg").readBytes().contentEquals(raw2) shouldBe true
    }

    @Test
    fun `rollback after two activates swaps current and previous`() {
        val store = BundleStore(context)
        val (raw1, v1) = verified(version = "v1", patterns = "patterns-v1".toByteArray())
        val (raw2, v2) = verified(version = "v2", patterns = "patterns-v2".toByteArray())

        store.activate(raw1, v1).isSuccess shouldBe true
        store.activate(raw2, v2).isSuccess shouldBe true
        store.rollback().isSuccess shouldBe true

        val current = File(context.filesDir, "sync/current")
        val previous = File(context.filesDir, "sync/previous")
        File(current, "bundle.afpkg").readBytes().contentEquals(raw1) shouldBe true
        File(previous, "bundle.afpkg").readBytes().contentEquals(raw2) shouldBe true
    }

    @Test
    fun `rollback when previous is absent fails with NoPreviousBundle`() {
        val store = BundleStore(context)
        val (raw1, v1) = verified()
        store.activate(raw1, v1).isSuccess shouldBe true

        val r = store.rollback()
        r.isFailure shouldBe true
        (r.exceptionOrNull() is BundleStoreError.NoPreviousBundle) shouldBe true
    }

    @Test
    fun `rollback twice — second rollback restores the original previous`() {
        val store = BundleStore(context)
        val (raw1, v1) = verified(version = "v1", patterns = "patterns-v1".toByteArray())
        val (raw2, v2) = verified(version = "v2", patterns = "patterns-v2".toByteArray())
        store.activate(raw1, v1)
        store.activate(raw2, v2)
        store.rollback()
        store.rollback()
        val current = File(context.filesDir, "sync/current")
        File(current, "bundle.afpkg").readBytes().contentEquals(raw2) shouldBe true
    }

    @Test
    fun `wipe deletes the sync directory completely`() {
        val store = BundleStore(context)
        val (raw1, v1) = verified()
        store.activate(raw1, v1).isSuccess shouldBe true

        store.wipe().isSuccess shouldBe true

        File(context.filesDir, "sync").exists() shouldBe false
    }

    @Test
    fun `wipe is idempotent — calling on empty store succeeds`() {
        val store = BundleStore(context)
        store.wipe().isSuccess shouldBe true
        store.wipe().isSuccess shouldBe true
        File(context.filesDir, "sync").exists() shouldBe false
    }
}
