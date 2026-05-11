@file:Suppress("DEPRECATION")

package com.qalqan.antifraud.sms

import android.provider.Telephony
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.SmsEntryDigest
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
 * Spec §23 #30 — a content-provider sweep on app launch picks up an SMS that arrived
 * while the app process was killed. Simulated here: the inbox cursor has a row, the
 * repository starts empty, the sweep is invoked once, the row is persisted.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance30SweepRecoveryTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()
    private val digest = SmsEntryDigest.create(context, box)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `inbox row missed during standby is recovered on next sweep`() {
        val cursor =
            RoboCursor().apply {
                setColumnNames(SmsContentProviderReader.PROJECTION.toList())
                setResults(
                    arrayOf(
                        arrayOf<Any?>(
                            "HALYKBANK",
                            "Перевод 5000 KZT",
                            1_700_000_000_000L,
                            0,
                            42L,
                        ),
                    ),
                )
            }
        shadowOf(context.contentResolver).setCursor(Telephony.Sms.Inbox.CONTENT_URI, cursor)

        runBlocking {
            // Pre-sweep: repository is empty (the broadcast was missed).
            repos.sms.listSince(Instant.EPOCH).isEmpty() shouldBe true

            val capture = AutoSmsCapture(SmsEventBuilder(digest, box), repos.sms)
            val sweeper =
                SmsContentProviderSweeper(
                    reader = SmsContentProviderReader(context.contentResolver),
                    capture = capture,
                )
            sweeper.sweepSince(0L)

            // Post-sweep: the missed SMS is persisted.
            val saved = repos.sms.listSince(Instant.EPOCH).single()
            saved.senderDisplayNameLocal shouldBe "HALYKBANK"
        }
    }
}
