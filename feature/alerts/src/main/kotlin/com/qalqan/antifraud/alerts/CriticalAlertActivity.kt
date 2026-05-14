package com.qalqan.antifraud.alerts

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/**
 * Spec §17.0.1 — full-screen alert target. Posted via `Notification.fullScreenIntent`
 * by [AlertNotificationBuilder]. Shows over the lock screen on Android 8.1+ via the
 * manifest attribute `android:showOnLockScreen="true"` + the legacy `setShowWhenLocked` /
 * `setTurnScreenOn` calls on older devices.
 *
 * Intent extras:
 *   - [EXTRA_REASONS]   array of strings (size ≥ 3 — checked downstream by [AlertContent])
 *   - [EXTRA_CAMPAIGN_ID] optional campaign id for "Pause and verify" deep-link
 */
class CriticalAlertActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        val reasons = intent.getStringArrayExtra(EXTRA_REASONS)?.toList().orEmpty()
        val campaignId = intent.getStringExtra(EXTRA_CAMPAIGN_ID)
        val content = AlertContent(reasons = reasons)
        setContent {
            CriticalAlertScreen(
                content = content,
                onPause = { onPause(campaignId) },
                onDismiss = { onDismiss(campaignId) },
            )
        }
    }

    private fun onPause(campaignId: String?) {
        val intent =
            Intent().apply {
                setClassName(applicationContext, MAIN_ACTIVITY_CLASS)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                campaignId?.let { putExtra(EXTRA_CAMPAIGN_ID, it) }
            }
        startActivity(intent)
        finish()
    }

    private fun onDismiss(campaignId: String?) {
        campaignId?.let { DismissalCooldown.record(it) }
        finish()
    }

    companion object {
        const val EXTRA_REASONS = "com.qalqan.antifraud.alerts.EXTRA_REASONS"
        const val EXTRA_CAMPAIGN_ID = "com.qalqan.antifraud.alerts.EXTRA_CAMPAIGN_ID"

        // Resolved via Intent.setClassName at runtime so `:feature:alerts` does not
        // need a Gradle dependency on `:app`. Adjust if MainActivity is renamed.
        private const val MAIN_ACTIVITY_CLASS = "com.qalqan.antifraud.MainActivity"
    }
}
