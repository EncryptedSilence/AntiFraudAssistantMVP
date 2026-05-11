@file:Suppress("DEPRECATION")

package com.qalqan.antifraud.acceptance

import android.provider.Telephony
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.SmsEntryDigest
import com.qalqan.antifraud.sms.AutoSmsCapture
import com.qalqan.antifraud.sms.SmsContentProviderReader
import com.qalqan.antifraud.sms.SmsContentProviderSweeper
import com.qalqan.antifraud.sms.SmsEventBuilder
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
 * Spec §23 #29 — auto SMS capture: a synthetic SMS in the inbox produces an SmsEvent with
 * correct sender / body excerpt (≤ 200 chars) / feature flags within 2 s of arrival.
 * Stage 4 verifies the *processing* latency in unit time; the wall-clock target is
 * verified end-to-end on a device in Stage 9.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance29AutoSmsCaptureTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()
    private val digest = SmsEntryDigest.create(context, box)

    @After fun tearDown() {
        repos.close()
    }

    @Test
    fun `synthetic inbox row lands in repo under 800ms with correct fields`() {
        val cursor =
            RoboCursor().apply {
                setColumnNames(SmsContentProviderReader.PROJECTION.toList())
                setResults(
                    arrayOf(
                        arrayOf<Any?>(
                            "HALYKBANK",
                            "Перевод 5000 KZT — code 123456",
                            1_700_000_000_000L,
                            0,
                            42L,
                        ),
                    ),
                )
            }
        shadowOf(context.contentResolver).setCursor(Telephony.Sms.Inbox.CONTENT_URI, cursor)

        runBlocking {
            val sweeper =
                SmsContentProviderSweeper(
                    reader = SmsContentProviderReader(context.contentResolver),
                    capture = AutoSmsCapture(SmsEventBuilder(digest, box), repos.sms),
                )
            val t0 = System.nanoTime()
            sweeper.sweepSince(0L)
            val elapsedMs = (System.nanoTime() - t0) / 1_000_000

            elapsedMs shouldBeLessThan ACCEPTANCE_29_BUDGET_MS

            val saved = repos.sms.listSince(Instant.EPOCH).single()
            saved.senderDisplayNameLocal shouldBe "HALYKBANK"
            saved.containsCode shouldBe true
            saved.containsFinancialKeyword shouldBe true
            // §16.3 — body excerpt cipher must stay under the 512-byte AEAD cap.
            (saved.bodyExcerptEnc.size <= 512) shouldBe true
        }
    }

    private companion object {
        // §23 #29's wall-clock budget is 2000 ms; we run on a CI box without telephony delay,
        // so we tighten the unit-test budget to 800 ms (matches Stage 3 T43's ceiling).
        const val ACCEPTANCE_29_BUDGET_MS = 800L
    }
}
