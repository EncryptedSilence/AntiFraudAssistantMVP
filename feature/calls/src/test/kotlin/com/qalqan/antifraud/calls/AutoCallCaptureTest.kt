@file:Suppress("DEPRECATION")

package com.qalqan.antifraud.calls

import android.provider.CallLog
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.CallEntryDigest
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.fakes.RoboCursor
import java.time.Instant

/**
 * RoboCursor / ShadowContentResolver.setCursor(Uri, BaseCursor) are deprecated in
 * Robolectric 4.13 in favour of registering a full ContentProvider stub. We suppress
 * the deprecation here because the replacement API adds significant boilerplate for a
 * read-only unit test, and the behaviour under test is unchanged.
 */
@RunWith(RobolectricTestRunner::class)
class AutoCallCaptureTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()

    @After
    fun tearDown() {
        repos.close()
    }

    // Helper: build a RoboCursor backed by CallLogReader.PROJECTION columns.
    private fun roboCursor(vararg rows: Array<Any?>): RoboCursor =
        RoboCursor().also { c ->
            c.setColumnNames(CallLogReader.PROJECTION.toMutableList())
            c.setResults(rows as Array<Array<Any?>>)
        }

    @Test
    fun `onIdle reads the latest CallLog row and persists a CallEvent`() {
        val cursor = roboCursor(
            arrayOf("+71112223344", CallLog.Calls.INCOMING_TYPE, 1_700_000_000_000L, 73L, null),
        )
        shadowOf(context.contentResolver).setCursor(CallLog.Calls.CONTENT_URI, cursor)

        runBlocking {
            val capture = AutoCallCapture(
                reader = CallLogReader(context.contentResolver),
                builder = CallEventBuilder(
                    digest = CallEntryDigest.create(context, box),
                    contacts = IsKnownContactResolver(repos.contacts),
                    repeats = RepeatCallDetector(repos.calls),
                ),
                calls = repos.calls,
            )
            capture.onIdle(simSlot = null)

            val saved = repos.calls.listSince(Instant.EPOCH)
            saved.size shouldBe 1
            saved.first().durationSec shouldBe 73L
            saved.first().direction.toString() shouldBe "INCOMING"
            saved.first().phoneHash.value.length shouldBe 64
        }
    }

    @Test
    fun `onIdle is a no-op when CallLog is empty`() {
        runBlocking {
            val capture = AutoCallCapture(
                reader = CallLogReader(context.contentResolver),
                builder = CallEventBuilder(
                    digest = CallEntryDigest.create(context, box),
                    contacts = IsKnownContactResolver(repos.contacts),
                    repeats = RepeatCallDetector(repos.calls),
                ),
                calls = repos.calls,
            )
            capture.onIdle(simSlot = null)
            repos.calls.listSince(Instant.EPOCH).isEmpty() shouldBe true
        }
    }
}
