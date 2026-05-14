package com.qalqan.antifraud.ui.references

import android.app.Application
import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.io.File

/**
 * Spec §17.4 — the last-bundle line in the References screen reads `createdAt` + `source`
 * from the current bundle manifest. Absent manifest → null timestamp.
 */
@RunWith(RobolectricTestRunner::class)
class ReferencesBundleManifestTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)
    private val viewModel =
        ReferencesViewModel(
            application = context.applicationContext as Application,
            repos = repos,
        )

    @After
    fun tearDown() {
        shadowOf(Looper.getMainLooper()).idle()
        repos.close()
        File(context.filesDir, "sync/current/manifest.json").delete()
    }

    private fun await(condition: () -> Boolean) {
        repeat(100) {
            shadowOf(Looper.getMainLooper()).idle()
            if (condition()) return
            Thread.sleep(5)
        }
    }

    @Test
    fun `lastBundleAt and lastBundleSource come from the current manifest when present`() {
        val syncDir = File(context.filesDir, "sync/current").apply { mkdirs() }
        // Stage 6 manifest fields: version, createdAt (ISO-8601), source, schemaVersion,
        // minAppVersion, priority, contents (paths starting with `data/`, sha256-prefixed).
        val sha = "sha256:" + "a".repeat(64)
        File(syncDir, "manifest.json").writeText(
            """
            {
              "version": "1.0.0",
              "createdAt": "2026-04-01T00:00:00Z",
              "source": "local",
              "schemaVersion": 1,
              "minAppVersion": 1,
              "priority": "normal",
              "previousPackageId": null,
              "contents": { "data/patterns.json": "$sha" }
            }
            """.trimIndent(),
        )
        viewModel.refresh()
        await { viewModel.state.value.lastBundleAt != null }
        viewModel.state.value.lastBundleAt shouldBe java.time.Instant.parse("2026-04-01T00:00:00Z")
        viewModel.state.value.lastBundleSource shouldBe "local"
    }

    @Test
    fun `lastBundleAt is null when no manifest is present`() {
        viewModel.refresh()
        await { !viewModel.state.value.isLoading && viewModel.state.value.smsCategories.isNotEmpty() }
        viewModel.state.value.lastBundleAt shouldBe null
    }
}
