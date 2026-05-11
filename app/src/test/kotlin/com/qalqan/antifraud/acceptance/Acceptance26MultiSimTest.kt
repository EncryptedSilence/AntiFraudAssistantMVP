@file:Suppress("DEPRECATION")

package com.qalqan.antifraud.acceptance

import android.content.Context
import android.os.Build
import android.provider.CallLog
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.calls.AutoCallCapture
import com.qalqan.antifraud.calls.CallEventBuilder
import com.qalqan.antifraud.calls.CallLogReader
import com.qalqan.antifraud.calls.IsKnownContactResolver
import com.qalqan.antifraud.calls.RepeatCallDetector
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
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboCursor
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
class Acceptance26MultiSimTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()

    @After fun tearDown() {
        repos.close()
    }

    private fun roboCursor(vararg rows: Array<Any?>): RoboCursor =
        RoboCursor().also { c ->
            c.setColumnNames(CallLogReader.PROJECTION.toMutableList())
            c.setResults(rows as Array<Array<Any?>>)
        }

    @Test
    fun `slot 0 propagates into persisted CallEvent`() {
        val cursor =
            roboCursor(
                arrayOf("+71112223344", CallLog.Calls.INCOMING_TYPE, 1L, 30L, null),
            )
        shadowOf(context.contentResolver).setCursor(CallLog.Calls.CONTENT_URI, cursor)

        runBlocking {
            val capture =
                AutoCallCapture(
                    reader = CallLogReader(context.contentResolver),
                    builder =
                        CallEventBuilder(
                            digest = CallEntryDigest.create(context, box),
                            contacts = IsKnownContactResolver(repos.contacts),
                            repeats = RepeatCallDetector(repos.calls),
                        ),
                    calls = repos.calls,
                )
            capture.onIdle(simSlot = 0)
            repos.calls.listSince(Instant.EPOCH).single().simSlot shouldBe 0
        }
    }

    @Test
    fun `slot 1 propagates into persisted CallEvent`() {
        val cursor =
            roboCursor(
                arrayOf("+71112223344", CallLog.Calls.INCOMING_TYPE, 1L, 30L, null),
            )
        shadowOf(context.contentResolver).setCursor(CallLog.Calls.CONTENT_URI, cursor)

        runBlocking {
            val capture =
                AutoCallCapture(
                    reader = CallLogReader(context.contentResolver),
                    builder =
                        CallEventBuilder(
                            digest = CallEntryDigest.create(context, box),
                            contacts = IsKnownContactResolver(repos.contacts),
                            repeats = RepeatCallDetector(repos.calls),
                        ),
                    calls = repos.calls,
                )
            capture.onIdle(simSlot = 1)
            repos.calls.listSince(Instant.EPOCH).single().simSlot shouldBe 1
        }
    }
}
