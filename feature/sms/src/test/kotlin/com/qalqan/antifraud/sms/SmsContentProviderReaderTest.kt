@file:Suppress("DEPRECATION") // RoboCursor + setCursor — see CallLogReaderTest precedent

package com.qalqan.antifraud.sms

import android.content.Context
import android.provider.Telephony
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.fakes.RoboCursor

@RunWith(RobolectricTestRunner::class)
class SmsContentProviderReaderTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `readSince returns empty when inbox cursor is empty`() {
        val reader = SmsContentProviderReader(context.contentResolver)
        reader.readSince(sinceMs = 0L) shouldBe emptyList()
    }

    @Test
    fun `readSince maps rows to InboxRow`() {
        val cursor =
            roboCursor(
                arrayOf<Any?>("+71112223344", "Hello", 1_700_000_000_000L, 1, 42L),
                arrayOf<Any?>("1414", "Citizen alert", 1_700_000_001_000L, 0, 43L),
            )
        shadowOf(context.contentResolver).setCursor(Telephony.Sms.Inbox.CONTENT_URI, cursor)

        val rows = SmsContentProviderReader(context.contentResolver).readSince(sinceMs = 0L)
        rows.size shouldBe 2
        rows[0].rawSender shouldBe "+71112223344"
        rows[0].body shouldBe "Hello"
        rows[0].receivedAtMs shouldBe 1_700_000_000_000L
        rows[0].subscriptionId shouldBe 1
        rows[0].providerRowId shouldBe 42L
        rows[1].rawSender shouldBe "1414"
        rows[1].subscriptionId shouldBe 0
    }

    private fun roboCursor(vararg rows: Array<Any?>): RoboCursor =
        RoboCursor().apply {
            setColumnNames(SmsContentProviderReader.PROJECTION.toList())
            setResults(rows.toList().toTypedArray())
        }
}
