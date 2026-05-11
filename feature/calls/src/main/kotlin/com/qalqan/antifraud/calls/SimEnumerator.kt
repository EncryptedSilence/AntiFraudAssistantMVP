@file:Suppress("ReturnCount")

package com.qalqan.antifraud.calls

import android.content.Context
import android.os.Build
import android.telephony.SubscriptionManager

/**
 * Spec §4.2.1 — multi-SIM. On Android 12+ each `SubscriptionInfo` carries a `simSlotIndex`
 * (0 or 1 on dual-SIM). The map is read once at service start (no live re-registration);
 * a hot-swap re-registration loop is deferred until Stage 4.
 */
class SimEnumerator(private val context: Context) {
    fun slotsBySubscriptionId(): Map<Int, Int> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return emptyMap()
        val sm =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                ?: return emptyMap()
        val infos = runCatching { sm.activeSubscriptionInfoList }.getOrNull() ?: return emptyMap()
        return infos.associate { it.subscriptionId to it.simSlotIndex }
    }
}
