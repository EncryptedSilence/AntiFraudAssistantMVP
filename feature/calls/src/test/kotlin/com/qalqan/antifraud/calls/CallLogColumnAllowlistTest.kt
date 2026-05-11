package com.qalqan.antifraud.calls

import android.provider.CallLog
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import org.junit.Test

/**
 * Privacy boundary: §2.1 / §4.2.1 — we only read the columns required to populate `CallEvent`.
 * Reading TRANSCRIPTION / VOICEMAIL_URI / LOCATION crosses the §2.1 prohibition on voice content.
 *
 * This test pins `CallLogReader.PROJECTION`. If you add a column, the spec must justify it.
 */
class CallLogColumnAllowlistTest {
    @Test
    fun `projection contains exactly the five allowlisted columns`() {
        CallLogReader.PROJECTION.toList() shouldContainExactlyInAnyOrder
            listOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.PHONE_ACCOUNT_ID,
            )
    }

    @Test
    fun `projection does not contain forbidden voice-content columns`() {
        val proj = CallLogReader.PROJECTION.toList()
        proj shouldNotContain CallLog.Calls.TRANSCRIPTION
        proj shouldNotContain CallLog.Calls.VOICEMAIL_URI
        proj shouldNotContain CallLog.Calls.GEOCODED_LOCATION
        proj shouldNotContain CallLog.Calls.CACHED_NAME
        proj shouldNotContain CallLog.Calls.CACHED_NUMBER_LABEL
        proj shouldNotContain CallLog.Calls.CACHED_NUMBER_TYPE
    }
}
