package com.qalqan.antifraud.calls

import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BatteryOptimizationPromptTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `intent action targets ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`() {
        val intent = BatteryOptimizationPrompt.intent(context)
        intent.action shouldBe Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data?.toString() shouldBe "package:${context.packageName}"
    }

    @Test
    fun `isExempt returns true when PowerManager reports the package as ignored`() {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        org.robolectric.Shadows.shadowOf(pm).setIgnoringBatteryOptimizations(context.packageName, true)
        BatteryOptimizationPrompt.isExempt(context) shouldBe true
    }

    @Test
    fun `isExempt returns false by default`() {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        org.robolectric.Shadows.shadowOf(pm).setIgnoringBatteryOptimizations(context.packageName, false)
        BatteryOptimizationPrompt.isExempt(context) shouldBe false
    }
}
