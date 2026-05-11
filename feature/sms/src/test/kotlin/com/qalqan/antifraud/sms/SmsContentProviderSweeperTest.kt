@file:Suppress("DEPRECATION")

package com.qalqan.antifraud.sms

import android.content.Context
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

@RunWith(RobolectricTestRunner::class)
class SmsContentProviderSweeperTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()
    private val digest = SmsEntryDigest.create(context, box)
    private val builder = SmsEventBuilder(digest, box)
    private val capture = AutoSmsCapture(builder, repos.sms)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `sweepSince persists every fresh inbox row`() {
        stubInboxWithTwoRows(context)
        runBlocking {
            SmsContentProviderSweeper(
                reader = SmsContentProviderReader(context.contentResolver),
                capture = capture,
            ).sweepSince(0L)
            repos.sms.listSince(Instant.EPOCH).size shouldBe 2
        }
    }

    @Test
    fun `sweepSince does not duplicate against rows already in the repo (via AutoSmsCapture dedup)`() {
        stubInboxWithTwoRows(context)
        val sweeper =
            SmsContentProviderSweeper(
                reader = SmsContentProviderReader(context.contentResolver),
                capture = capture,
            )
        runBlocking {
            sweeper.sweepSince(0L)
            // Second sweep over the same inbox rows: dedup should drop the duplicates.
            sweeper.sweepSince(0L)
            repos.sms.listSince(Instant.EPOCH).size shouldBe 2
        }
    }

    private fun stubInboxWithTwoRows(context: Context) {
        val cursor =
            RoboCursor().apply {
                setColumnNames(SmsContentProviderReader.PROJECTION.toList())
                setResults(
                    arrayOf(
                        arrayOf<Any?>(
                            "+71112223344",
                            "Hello",
                            1_700_000_000_000L,
                            1,
                            42L,
                        ),
                        arrayOf<Any?>(
                            "1414",
                            "Citizen alert",
                            1_700_000_001_000L,
                            0,
                            43L,
                        ),
                    ),
                )
            }
        shadowOf(context.contentResolver).setCursor(Telephony.Sms.Inbox.CONTENT_URI, cursor)
    }
}
