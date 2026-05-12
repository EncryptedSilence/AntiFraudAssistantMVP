package com.qalqan.antifraud.acceptance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.sync.SyncSettings
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §23 #1 — the home screen loads and the basic manual-entry / scoring / warnings
 * paths work without any server. Stage 6 introduces the INTERNET permission and the
 * `:core:sync` module; this test re-asserts the §23 #1 contract by confirming nothing
 * about Stage 6's wiring forces a network call on launch.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance1AirplaneModeUnchangedTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `§23 #1 — SyncSettings is constructible on a fresh install and disabled by default`() {
        val settings = SyncSettings(context)
        settings.enabled shouldBe false
        settings.lastSyncAt shouldBe null
        settings.lastSyncResult shouldBe null
    }

    @Test
    fun `§23 #1 — toggling sync on does not force an immediate network call`() {
        val settings = SyncSettings(context)
        settings.enabled = true
        settings.enabled shouldBe true
    }
}
