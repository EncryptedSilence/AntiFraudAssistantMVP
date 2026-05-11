package com.qalqan.antifraud.calls

import android.content.Context
import android.os.Build
import android.telephony.SubscriptionManager
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S]) // Android 12
class SimEnumeratorTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `empty subscription list returns empty map`() {
        SimEnumerator(context).slotsBySubscriptionId() shouldBe emptyMap()
    }

    @Test
    fun `dual-SIM SubscriptionManager returns two entries`() {
        val sm = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        shadowOf(sm).setActiveSubscriptionInfoList(
            listOf(
                org.robolectric.shadows.ShadowSubscriptionManager
                    .SubscriptionInfoBuilder.newBuilder()
                    .setId(1).setSimSlotIndex(0).buildSubscriptionInfo(),
                org.robolectric.shadows.ShadowSubscriptionManager
                    .SubscriptionInfoBuilder.newBuilder()
                    .setId(2).setSimSlotIndex(1).buildSubscriptionInfo(),
            ),
        )

        val slots = SimEnumerator(context).slotsBySubscriptionId()
        slots[1] shouldBe 0
        slots[2] shouldBe 1
    }
}
