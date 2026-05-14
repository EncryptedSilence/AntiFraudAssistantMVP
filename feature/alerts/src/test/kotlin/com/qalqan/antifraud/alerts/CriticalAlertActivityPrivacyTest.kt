package com.qalqan.antifraud.alerts

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import io.kotest.assertions.throwables.shouldThrow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CriticalAlertActivityPrivacyTest {
    @Test
    fun `activity refuses reasons containing a phone number`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val intent =
            Intent(ctx, CriticalAlertActivity::class.java).apply {
                putExtra(
                    CriticalAlertActivity.EXTRA_REASONS,
                    arrayOf("Suspicious +77001234567 was on the line.", "Reason 2", "Reason 3"),
                )
            }
        shouldThrow<IllegalArgumentException> {
            Robolectric.buildActivity(CriticalAlertActivity::class.java, intent).setup()
        }
    }

    @Test
    fun `activity refuses reasons containing a domain`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val intent =
            Intent(ctx, CriticalAlertActivity::class.java).apply {
                putExtra(
                    CriticalAlertActivity.EXTRA_REASONS,
                    arrayOf("Visit kaspi-bonus.kz now.", "Reason 2", "Reason 3"),
                )
            }
        shouldThrow<IllegalArgumentException> {
            Robolectric.buildActivity(CriticalAlertActivity::class.java, intent).setup()
        }
    }

    @Test
    fun `activity refuses reasons containing an OTP`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val intent =
            Intent(ctx, CriticalAlertActivity::class.java).apply {
                putExtra(
                    CriticalAlertActivity.EXTRA_REASONS,
                    arrayOf("Your code 8421 was requested.", "Reason 2", "Reason 3"),
                )
            }
        shouldThrow<IllegalArgumentException> {
            Robolectric.buildActivity(CriticalAlertActivity::class.java, intent).setup()
        }
    }
}
