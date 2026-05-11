package com.qalqan.antifraud.calls

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

/**
 * Spec §22 Stage 3 — battery-optimization-exemption prompt on first run.
 * The intent opens the system settings page where the user can grant the exemption;
 * we never bypass the user. `isExempt` reports the post-decision state.
 */
object BatteryOptimizationPrompt {
    @SuppressLint("BatteryLife")
    fun intent(context: Context): Intent =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            .setData(Uri.parse("package:${context.packageName}"))

    fun isExempt(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }
}
