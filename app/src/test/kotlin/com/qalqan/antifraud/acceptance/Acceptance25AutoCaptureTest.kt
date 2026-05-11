@file:Suppress("DEPRECATION")

package com.qalqan.antifraud.acceptance

import android.content.Context
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
import com.qalqan.antifraud.domain.CallDirection
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

@RunWith(RobolectricTestRunner::class)
class Acceptance25AutoCaptureTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()

    @After fun tearDown() { repos.close() }

    @Test
    fun `auto-captured call lands in the repo with correct fields under 2s budget`() {
        val cursor = RoboCursor().apply {
            setColumnNames(CallLogReader.PROJECTION.toList())
            setResults(arrayOf(arrayOf<Any?>("+71112223344", CallLog.Calls.INCOMING_TYPE, 1_700_000_000_000L, 73L, null)))
        }
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

            val t0 = System.nanoTime()
            capture.onIdle(simSlot = null)
            val elapsedMs = (System.nanoTime() - t0) / 1_000_000

            elapsedMs shouldBeLessThan ACCEPTANCE_25_BUDGET_MS

            val saved = repos.calls.listSince(Instant.EPOCH).single()
            saved.durationSec shouldBe 73L
            saved.direction shouldBe CallDirection.INCOMING
            saved.simSlot shouldBe null
            saved.phoneHash.value.length shouldBe 64
        }
    }

    private companion object {
        const val ACCEPTANCE_25_BUDGET_MS = 800L
    }
}
