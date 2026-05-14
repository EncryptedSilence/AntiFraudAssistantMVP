package com.qalqan.antifraud.alerts

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class CriticalAlertActivityPauseTest {
    @Test
    fun `Pause and verify launches MainActivity with the campaign id`() {
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

        // simulate the user tapping `Pause and verify`
        // We call the private onPause via reflection because the Compose tree is not
        // rendered under Robolectric and we want to test the Intent contract, not the UI.
        val m = CriticalAlertActivity::class.java.getDeclaredMethod("onPause", String::class.java)
        m.isAccessible = true
        m.invoke(activity, "camp-42")

        val started = shadowOf(activity).nextStartedActivity
        val expected = ComponentName(ctx.packageName, "com.qalqan.antifraud.MainActivity")
        started.component shouldBe expected
        started.getStringExtra(CriticalAlertActivity.EXTRA_CAMPAIGN_ID) shouldBe "camp-42"
    }
}
