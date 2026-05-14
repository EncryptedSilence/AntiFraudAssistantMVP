package com.qalqan.antifraud.alerts

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/**
 * Spec §17.0.2 — thin always-on-top banner. Two lines max, two buttons, auto-dismiss
 * after 30 seconds. Never shows raw numbers / domains / SMS bodies; the body lines come
 * from [AlertContent], which enforces redaction at construction.
 *
 * The activity uses `FLAG_NOT_TOUCH_MODAL` so the underlying app remains interactive;
 * `FLAG_LAYOUT_NO_LIMITS` lets the banner sit at the top edge without insets math.
 */
class OverlayBannerActivity : ComponentActivity() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val autoDismiss = Runnable { onDismiss(currentCampaignId) }
    private var currentCampaignId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        )
        val reasons = intent.getStringArrayExtra(EXTRA_REASONS)?.toList().orEmpty()
        currentCampaignId = intent.getStringExtra(EXTRA_CAMPAIGN_ID)
        val content = AlertContent(reasons = reasons)
        setContent {
            OverlayBannerScreen(
                content = content,
                onPause = { onPause(currentCampaignId) },
                onDismiss = { onDismiss(currentCampaignId) },
            )
        }
        mainHandler.postDelayed(autoDismiss, AUTO_DISMISS_MS)
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(autoDismiss)
        super.onDestroy()
    }

    private fun onPause(campaignId: String?) {
        mainHandler.removeCallbacks(autoDismiss)
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
        mainHandler.removeCallbacks(autoDismiss)
        campaignId?.let { DismissalCooldown.record(it) }
        finish()
    }

    companion object {
        const val EXTRA_REASONS = "com.qalqan.antifraud.alerts.overlay.EXTRA_REASONS"
        const val EXTRA_CAMPAIGN_ID = "com.qalqan.antifraud.alerts.overlay.EXTRA_CAMPAIGN_ID"
        const val AUTO_DISMISS_MS = 30_000L

        private const val MAIN_ACTIVITY_CLASS = "com.qalqan.antifraud.MainActivity"
    }
}
