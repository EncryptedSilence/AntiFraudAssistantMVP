@file:Suppress("MultipleTopLevelClassesInFile", "ReturnCount", "MagicNumber")

package com.qalqan.antifraud.calls

import android.content.ContentResolver
import android.os.Build
import android.os.Bundle
import android.provider.CallLog

/**
 * Spec §4.2.1 — read NUMBER / TYPE / DATE / DURATION / PHONE_ACCOUNT_ID columns only.
 * §2.1 forbids reading TRANSCRIPTION, VOICEMAIL_URI, LOCATION; this allowlist is the
 * privacy boundary and is enforced by the test in T21.
 */
class CallLogReader(private val resolver: ContentResolver) {
    fun readLatest(): CallLogRow? {
        val cursor =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                resolver.query(
                    CallLog.Calls.CONTENT_URI,
                    PROJECTION,
                    Bundle().apply {
                        putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(CallLog.Calls.DATE))
                        putInt(
                            ContentResolver.QUERY_ARG_SORT_DIRECTION,
                            ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
                        )
                        putInt(ContentResolver.QUERY_ARG_LIMIT, 1)
                    },
                    null,
                )
            } else {
                @Suppress("DEPRECATION")
                resolver.query(
                    CallLog.Calls.CONTENT_URI,
                    PROJECTION,
                    null,
                    null,
                    "${CallLog.Calls.DATE} DESC",
                )
            } ?: return null
        cursor.use {
            if (!it.moveToFirst()) return null
            val number = it.getString(0) ?: ""
            val type = it.getInt(1)
            val startedAt = it.getLong(2)
            val durationSec = it.getLong(3)
            val phoneAccountId = it.getString(4)
            val direction =
                when (type) {
                    CallLog.Calls.INCOMING_TYPE -> CallLogRow.Direction.INCOMING
                    CallLog.Calls.OUTGOING_TYPE -> CallLogRow.Direction.OUTGOING
                    CallLog.Calls.MISSED_TYPE -> CallLogRow.Direction.MISSED
                    else -> CallLogRow.Direction.UNKNOWN
                }
            return CallLogRow(
                rawNumber = number,
                direction = direction,
                startedAtMs = startedAt,
                durationSec = durationSec,
                phoneAccountId = phoneAccountId,
            )
        }
    }

    companion object {
        val PROJECTION: Array<String> =
            arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.PHONE_ACCOUNT_ID,
            )
    }
}

data class CallLogRow(
    val rawNumber: String,
    val direction: Direction,
    val startedAtMs: Long,
    val durationSec: Long,
    val phoneAccountId: String?,
) {
    enum class Direction { INCOMING, OUTGOING, MISSED, UNKNOWN }
}
