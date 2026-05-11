@file:Suppress("DEPRECATION")

package com.qalqan.antifraud.calls

import android.content.Context
import android.os.Build
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
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboCursor
import java.time.Instant

/**
 * Spec §23 #26 — on a dual-SIM device, calls on either SIM produce a `CallEvent` with the
 * correct `simSlot`. Stage 3 verifies the wiring through `SimEnumerator` →
 * `CallStateRouter.register(subscriptionIds=...)` → `AutoCallCapture.onIdle(simSlot=...)`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S]) // Android 12
class Acceptance26MultiSimTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
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
    fun `simSlot of 0 propagates from subscription 1 into the persisted CallEvent`() {
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
    fun `simSlot of 1 propagates from subscription 2`() {
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
