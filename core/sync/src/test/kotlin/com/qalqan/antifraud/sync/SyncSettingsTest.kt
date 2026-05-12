package com.qalqan.antifraud.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class SyncSettingsTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `default enabled is false (§23 #4 hold by construction)`() {
        val s = SyncSettings(context)
        s.enabled shouldBe false
    }

    @Test
    fun `enabled persists across instances`() {
        SyncSettings(context).enabled = true
        SyncSettings(context).enabled shouldBe true
    }

    @Test
    fun `lastSyncAt round-trips Instant via epoch millis`() {
        val s = SyncSettings(context)
        s.lastSyncAt shouldBe null
        val t = Instant.parse("2026-05-12T10:00:00Z")
        s.lastSyncAt = t
        SyncSettings(context).lastSyncAt shouldBe t
    }

    @Test
    fun `lastSyncResult round-trips a String`() {
        val s = SyncSettings(context)
        s.lastSyncResult shouldBe null
        s.lastSyncResult = "ok"
        SyncSettings(context).lastSyncResult shouldBe "ok"
    }
}
