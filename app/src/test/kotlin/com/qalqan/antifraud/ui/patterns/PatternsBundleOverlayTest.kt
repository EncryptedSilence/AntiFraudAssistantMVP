package com.qalqan.antifraud.ui.patterns

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
 * Spec §7 + §17.3 — a pattern present under `filesDir/sync/current/patterns/<id>.json`
 * overrides the in-APK seed of the same `patternId` and is rendered with
 * [PatternsUiState.Source.BUNDLE].
 */
@RunWith(RobolectricTestRunner::class)
class PatternsBundleOverlayTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repos = Repositories.inMemory(context)
    private val viewModel =
        PatternsViewModel(
            application = context.applicationContext as Application,
            repos = repos,
        )

    @After
    fun tearDown() {
        shadowOf(Looper.getMainLooper()).idle()
        repos.close()
    }

    private fun await() {
        repeat(100) {
            shadowOf(Looper.getMainLooper()).idle()
            if (!viewModel.state.value.isLoading && viewModel.state.value.rows.isNotEmpty()) return
            Thread.sleep(5)
        }
    }

    @Test
    fun `bundle JSON file with patternId 'authority_spoof_call_v1' tags the row as BUNDLE`() {
        val patternsDir = File(context.filesDir, "sync/current/patterns").apply { mkdirs() }
        File(patternsDir, "authority_spoof_call_v1.json").writeText(
            """
            {
              "patternId": "authority_spoof_call_v1",
              "name": "Bundled authority spoof v2",
              "description": "Bundled overlay test pattern.",
              "category": "authoritySpoof",
              "version": "2.0.0",
              "enabled": true,
              "userCreated": false,
              "source": "system",
              "conditions": [
                { "eventType": "CallEvent", "field": "isKnownContact", "operator": "equals", "value": false, "weight": 20 }
              ],
              "correlation": { "maxCampaignAgeDays": 14, "linkStrength": 0.9 },
              "warning": {
                "level": "high",
                "title": "Possible authority impersonation",
                "message": "..."
              },
              "recommendation": "..."
            }
            """.trimIndent(),
        )
        viewModel.refresh()
        await()
        val row =
            viewModel.state.value.rows
                .firstOrNull { it.patternId == "authority_spoof_call_v1" }
                ?: error("authority_spoof_call_v1 pattern not found")
        row.source shouldBe PatternsUiState.Source.BUNDLE
        row.version shouldBe "2.0.0"
        row.name shouldBe "Bundled authority spoof v2"
    }
}
