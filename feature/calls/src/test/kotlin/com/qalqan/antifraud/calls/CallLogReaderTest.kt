@file:Suppress("DEPRECATION")

package com.qalqan.antifraud.calls

import android.content.Context
import android.provider.CallLog
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.fakes.RoboCursor

/**
 * RoboCursor / ShadowContentResolver.setCursor(Uri, BaseCursor) are deprecated in
 * Robolectric 4.13 in favour of registering a full ContentProvider stub. We suppress
 * the deprecation here because the replacement API adds significant boilerplate for a
 * read-only unit test, and the behaviour under test is unchanged.
 */
@RunWith(RobolectricTestRunner::class)
class CallLogReaderTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `readLatest returns null when call log is empty`() {
        val reader = CallLogReader(context.contentResolver)
        reader.readLatest() shouldBe null
    }

    @Test
    fun `readLatest returns the most-recent row mapped into CallLogRow`() {
        val cursor =
            roboCursor(
                arrayOf("+71112223344", CallLog.Calls.INCOMING_TYPE, 1_700_000_000_000L, 73L, "0"),
            )
        shadowOf(context.contentResolver).setCursor(CallLog.Calls.CONTENT_URI, cursor)

        val row = CallLogReader(context.contentResolver).readLatest()
        row?.rawNumber shouldBe "+71112223344"
        row?.direction shouldBe CallLogRow.Direction.INCOMING
        row?.startedAtMs shouldBe 1_700_000_000_000L
        row?.durationSec shouldBe 73L
        row?.phoneAccountId shouldBe "0"
    }

    @Test
    fun `readLatest maps OUTGOING and MISSED types correctly`() {
        val cursor =
            roboCursor(
                arrayOf("+71112223344", CallLog.Calls.OUTGOING_TYPE, 1L, 0L, null),
                arrayOf("+71112223344", CallLog.Calls.MISSED_TYPE, 2L, 0L, null),
            )
        shadowOf(context.contentResolver).setCursor(CallLog.Calls.CONTENT_URI, cursor)

        // The shadow returns rows in insertion order; the first row is OUTGOING.
        val firstRow = CallLogReader(context.contentResolver).readLatest()
        firstRow?.direction shouldBe CallLogRow.Direction.OUTGOING
    }

    // Helper: build a RoboCursor (extends BaseCursor) backed by CallLogReader.PROJECTION columns.
    private fun roboCursor(vararg rows: Array<Any?>): RoboCursor =
        RoboCursor().also { c ->
            c.setColumnNames(CallLogReader.PROJECTION.toMutableList())
            c.setResults(rows as Array<Array<Any?>>)
        }
}
