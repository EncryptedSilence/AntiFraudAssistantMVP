package com.qalqan.antifraud.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.crypto.BundleManifest
import com.qalqan.antifraud.crypto.BundlePriority
import com.qalqan.antifraud.crypto.VerifiedBundle
import com.qalqan.antifraud.database.Repositories
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class RepositoriesWipeIntegrationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)
    private val store = BundleStore(context)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `wipeAll deletes the sync directory when a BundleStore is wired`() {
        val manifest =
            BundleManifest(
                version = "v1",
                createdAt = Instant.parse("2026-05-12T10:00:00Z"),
                source = "stable",
                schemaVersion = 1,
                minAppVersion = 1,
                priority = BundlePriority.NORMAL,
                previousPackageId = null,
                contents = mapOf("data/patterns.json" to "sha256:${"a".repeat(64)}"),
            )
        val v = VerifiedBundle(manifest, mapOf("data/patterns.json" to "x".toByteArray()))
        store.activate("raw".toByteArray(), v).isSuccess shouldBe true
        File(context.filesDir, "sync/current").exists() shouldBe true

        runBlocking { repos.wipeAll(bundleStore = store) }

        File(context.filesDir, "sync").exists() shouldBe false
    }
}
