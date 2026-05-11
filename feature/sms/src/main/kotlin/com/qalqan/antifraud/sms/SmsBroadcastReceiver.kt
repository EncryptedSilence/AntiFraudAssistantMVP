package com.qalqan.antifraud.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.qalqan.antifraud.calls.SimEnumerator
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.KeyStoreCryptoBox
import com.qalqan.antifraud.database.manual.SmsEntryDigest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Spec §4.2.2 — manifest-static receiver invoked by the system on every SMS arrival
 * (provided `RECEIVE_SMS` is granted and the OS has not killed our package). The receiver
 * itself is a thin shim: parse the intent, ask `captureProvider` for an `AutoSmsCapture`
 * + cleanup handle, map the simSlot via `SimEnumerator`, dispatch, close. Heavy work runs
 * in a `goAsync()` scope.
 *
 * Robolectric test path: tests replace `captureProvider` to bypass AndroidKeyStore, then
 * call `onSmsReceived(context, broadcast)` directly. Android's PDU plumbing is brittle in
 * Robolectric 4.13; full intent-decoding is covered by `SmsParserTest` (T10).
 */
class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val broadcast = SmsParser.extractFromIntent(intent) ?: return
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                onSmsReceived(context.applicationContext, broadcast)
            } finally {
                pendingResult.finish()
            }
        }
    }

    @VisibleForTesting
    internal suspend fun onSmsReceived(context: Context, broadcast: SmsBroadcast) {
        val handle = try {
            captureProvider(context)
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            // AndroidKeyStore unavailable or DB init failed; production should not hit this.
            android.util.Log.e(TAG, "SMS capture init failed; broadcast dropped", e)
            return
        }
        try {
            val mappedSlot = mapSimSlot(context, broadcast.simSlot)
            handle.capture.accept(broadcast.copy(simSlot = mappedSlot))
        } finally {
            handle.close()
        }
    }

    private fun mapSimSlot(context: Context, raw: Int?): Int? {
        if (raw == null) return null
        val slots = SimEnumerator(context).slotsBySubscriptionId()
        return slots[raw] ?: raw
    }

    /**
     * Pairing of [AutoSmsCapture] with a cleanup callback (typically a `Repositories.close`).
     * Production builds this from `Repositories.build` + `KeyStoreCryptoBox`; tests build it
     * from `Repositories.inMemory` + `InMemoryCryptoBox`.
     */
    class CaptureHandle(
        val capture: AutoSmsCapture,
        val close: () -> Unit,
    )

    companion object {
        private const val TAG = "SmsBroadcastReceiver"

        internal val defaultCaptureProvider: (Context) -> CaptureHandle = { ctx ->
            val r = Repositories.build(ctx)
            val digest = SmsEntryDigest.create(ctx)
            val box = KeyStoreCryptoBox.create(ctx, alias = "antifraud.field_box")
            CaptureHandle(
                capture = AutoSmsCapture(
                    builder = SmsEventBuilder(digest = digest, box = box),
                    sms = r.sms,
                ),
                close = { r.close() },
            )
        }

        @VisibleForTesting
        var captureProvider: (Context) -> CaptureHandle = defaultCaptureProvider
    }
}
