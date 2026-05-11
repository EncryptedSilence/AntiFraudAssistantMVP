@file:Suppress("DEPRECATION")

package com.qalqan.antifraud.calls

import android.provider.CallLog
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.CallEntryDigest
import io.kotest.matchers.longs.shouldBeLessThan
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
 * Spec §23 #25 — auto call capture: the IDLE transition must produce a `CallEvent` with the
 * correct number / direction / `durationSec` within 2 s of the call ending. Stage 3 verifies
 * the *processing* latency in unit time; the wall-clock target is verified end-to-end on a
 * device in Phase 9 (T43).
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance25LatencyTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()

    @After
    fun tearDown() {
        repos.close()
    }

    // Reuse the same cursor-stub pattern from AutoCallCaptureTest.
    private fun roboCursor(vararg rows: Array<Any?>): RoboCursor =
        RoboCursor().also { c ->
            c.setColumnNames(CallLogReader.PROJECTION.toMutableList())
            c.setResults(rows as Array<Array<Any?>>)
        }

    @Test
    fun `IDLE-to-persistence completes in well under 2s for a single call`() {
        val cursor =
            roboCursor(
                arrayOf("+71112223344", CallLog.Calls.INCOMING_TYPE, 1_700_000_000_000L, 73L, null),
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
            val t0 = System.nanoTime()
            capture.onIdle(simSlot = null)
            val elapsedMs = (System.nanoTime() - t0) / 1_000_000

            elapsedMs shouldBeLessThan ACCEPTANCE_25_BUDGET_MS
            val saved = repos.calls.listSince(Instant.EPOCH).single()
            saved.durationSec shouldBe 73L
        }
    }

    private companion object {
        // §23 #25 budget is 2 s wall-clock end-to-end. Unit-test budget is much tighter
        // because we have no real DB I/O or telephony delay; 500 ms is a generous ceiling
        // for a CI box.
        const val ACCEPTANCE_25_BUDGET_MS = 500L
    }
}
