package com.qalqan.antifraud.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.qalqan.antifraud.database.Repositories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * adb entry point to the debug-only [ChainSeeder]. Compiled only into debug builds (lives in
 * `src/debug`), so it never ships in release. The in-app button on the Campaigns screen reaches the
 * same seeder via [DebugHooks.seedDemoChains]; prefer that on a device without a cable.
 *
 * Trigger (debug build installed):
 *   adb shell am broadcast -n com.qalqan.antifraud/.debug.DebugSeedReceiver
 *
 * Then re-enter the Campaigns screen so its ViewModel re-queries. Use the existing
 * "Удалить все данные" (Privacy) to clear the seeded rows.
 */
class DebugSeedReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val pending = goAsync()
        val app = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repos = Repositories.build(app)
                try {
                    val count = ChainSeeder.seed(repos)
                    Log.i(TAG, "Seeded $count demo risk chains")
                } finally {
                    repos.close()
                }
            } catch (
                @Suppress("TooGenericExceptionCaught") e: Exception,
            ) {
                Log.e(TAG, "Demo chain seed failed", e)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "DebugSeed"
    }
}
