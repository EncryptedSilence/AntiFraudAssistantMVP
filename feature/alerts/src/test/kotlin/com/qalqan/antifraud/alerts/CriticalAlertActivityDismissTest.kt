package com.qalqan.antifraud.alerts

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CriticalAlertActivityDismissTest {
    @After
    fun clearCooldown() {
        DismissalCooldown.clearAllForTest()
    }

    @Test
    fun `Dismiss records cooldown for the campaign and finishes the activity`() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val intent =
            Intent(ctx, CriticalAlertActivity::class.java).apply {
                putExtra(
                    CriticalAlertActivity.EXTRA_REASONS,
                    arrayOf(
                        "Caller is not in your contacts.",
                        "Risk level reached critical.",
                        "Matched pattern: lookalike domain",
                    ),
                )
                putExtra(CriticalAlertActivity.EXTRA_CAMPAIGN_ID, "camp-42")
            }
        val activity = Robolectric.buildActivity(CriticalAlertActivity::class.java, intent).setup().get()

        val m = CriticalAlertActivity::class.java.getDeclaredMethod("onDismiss", String::class.java)
        m.isAccessible = true
        m.invoke(activity, "camp-42")

        DismissalCooldown.isCoolingDown("camp-42") shouldBe true
        activity.isFinishing shouldBe true
    }
}
