package com.qalqan.antifraud

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.qalqan.antifraud.calls.CallObserverPermissions
import com.qalqan.antifraud.calls.CallObserverService
import com.qalqan.antifraud.calls.SimEnumerator
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.KeyStoreCryptoBox
import com.qalqan.antifraud.database.manual.SmsEntryDigest
import com.qalqan.antifraud.sms.AutoSmsCapture
import com.qalqan.antifraud.sms.SmsContentProviderReader
import com.qalqan.antifraud.sms.SmsContentProviderSweeper
import com.qalqan.antifraud.sms.SmsEventBuilder
import com.qalqan.antifraud.sms.SmsObserverActionLog
import com.qalqan.antifraud.sms.SmsObserverPermissions
import com.qalqan.antifraud.sms.SmsSweepCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var sweepScope: CoroutineScope? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StatusScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        if (CallObserverPermissions(this).state() == CallObserverPermissions.State.GRANTED) {
            CallObserverService.start(this)
        }
        if (SmsObserverPermissions(this).state() == SmsObserverPermissions.State.GRANTED) {
            startSmsSweep()
        }
    }

    override fun onPause() {
        super.onPause()
        sweepScope?.cancel()
        sweepScope = null
    }

    private fun startSmsSweep() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        sweepScope = scope
        scope.launch {
            val r =
                try {
                    Repositories.build(applicationContext)
                } catch (
                    @Suppress("TooGenericExceptionCaught") e: Exception,
                ) {
                    Log.e(TAG, "SMS sweep init failed; sweep skipped", e)
                    return@launch
                }
            try {
                val digest = SmsEntryDigest.create(applicationContext)
                val box = KeyStoreCryptoBox.create(applicationContext, alias = "antifraud.field_box")
                val coord =
                    SmsSweepCoordinator(
                        sweeper =
                            SmsContentProviderSweeper(
                                reader = SmsContentProviderReader(applicationContext.contentResolver),
                                capture = AutoSmsCapture(SmsEventBuilder(digest, box), r.sms),
                                sims = SimEnumerator(applicationContext),
                            ),
                        actionLog = SmsObserverActionLog(r.actionLogger),
                    )
                coord.runOneShot(System.currentTimeMillis() - SWEEP_LOOKBACK_MS)
                while (true) {
                    delay(SWEEP_INTERVAL_MS)
                    coord.runOneShot(System.currentTimeMillis() - SWEEP_LOOKBACK_MS)
                }
            } finally {
                r.close()
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val SWEEP_LOOKBACK_MS = 24L * 60L * 60L * 1000L
        private const val SWEEP_INTERVAL_MS = 60L * 1000L
    }
}
