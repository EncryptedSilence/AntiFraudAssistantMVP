@file:Suppress("MultipleTopLevelClassesInFile", "ReturnCount")

package com.qalqan.antifraud.sms

import android.content.ContentResolver
import android.provider.Telephony

/**
 * Spec §4.2.2 + §2.1 — read ADDRESS / BODY / DATE / SUB_ID / _ID columns from
 * Telephony.Sms.Inbox only. NEVER reads MMS, outbox, sent, drafts, or undelivered
 * provider URIs. NEVER reads STATUS, TYPE, ERROR_CODE — those carry no signal we use.
 * The allowlist is the privacy boundary and is pinned by `SmsColumnAllowlistTest` (T19).
 */
class SmsContentProviderReader(private val resolver: ContentResolver) {

    fun readSince(sinceMs: Long): List<InboxRow> {
        val cursor = resolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            PROJECTION,
            // selection
            "${Telephony.Sms.DATE} >= ?",
            // selectionArgs
            arrayOf(sinceMs.toString()),
            // sortOrder
            "${Telephony.Sms.DATE} ASC",
        ) ?: return emptyList()
        val out = mutableListOf<InboxRow>()
        cursor.use {
            while (it.moveToNext()) {
                out.add(
                    InboxRow(
                        rawSender = it.getString(0) ?: "",
                        body = it.getString(1) ?: "",
                        receivedAtMs = it.getLong(2),
                        subscriptionId = it.getInt(3).takeIf { id -> id >= 0 },
                        providerRowId = it.getLong(4),
                    ),
                )
            }
        }
        return out
    }

    companion object {
        val PROJECTION: Array<String> = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.SUBSCRIPTION_ID,
            Telephony.Sms._ID,
        )
    }
}

data class InboxRow(
    val rawSender: String,
    val body: String,
    val receivedAtMs: Long,
    val subscriptionId: Int?,
    val providerRowId: Long,
)
