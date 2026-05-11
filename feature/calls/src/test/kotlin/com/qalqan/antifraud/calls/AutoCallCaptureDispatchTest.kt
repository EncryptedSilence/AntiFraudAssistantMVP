@file:Suppress("DEPRECATION")

package com.qalqan.antifraud.calls

import android.provider.CallLog
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.CallEntryDigest
import com.qalqan.antifraud.domain.CallEvent
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.fakes.RoboCursor
import java.util.concurrent.atomic.AtomicReference

@RunWith(RobolectricTestRunner::class)
class AutoCallCaptureDispatchTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()

    @After
    fun tearDown() {
        repos.close()
    }

    private fun roboCursor(vararg rows: Array<Any?>): RoboCursor =
        RoboCursor().also { c ->
            c.setColumnNames(CallLogReader.PROJECTION.toMutableList())
            c.setResults(rows as Array<Array<Any?>>)
        }

    @Test
    fun `onCaptured callback fires once with the persisted CallEvent`() {
        val cursor =
            roboCursor(
                arrayOf("+71112223344", CallLog.Calls.INCOMING_TYPE, 1L, 30L, null),
            )
        shadowOf(context.contentResolver).setCursor(CallLog.Calls.CONTENT_URI, cursor)

        val seen = AtomicReference<CallEvent?>()
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
                    onCaptured = { ev -> seen.set(ev) },
                )
            capture.onIdle(simSlot = null)
        }
        seen.get()?.durationSec shouldBe 30L
    }
}
