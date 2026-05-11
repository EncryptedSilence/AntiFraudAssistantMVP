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

@RunWith(RobolectricTestRunner::class)
class SmsSweepCoordinatorTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()
    private val digest = SmsEntryDigest.create(context, box)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `runOneShot performs a single sweep and returns`() {
        val cursor =
            RoboCursor().apply {
                setColumnNames(SmsContentProviderReader.PROJECTION.toList())
                setResults(arrayOf(arrayOf<Any?>("S", "Body", 1L, 0, 1L)))
            }
        shadowOf(context.contentResolver).setCursor(Telephony.Sms.Inbox.CONTENT_URI, cursor)

        val coord =
            SmsSweepCoordinator(
                sweeper =
                    SmsContentProviderSweeper(
                        reader = SmsContentProviderReader(context.contentResolver),
                        capture = AutoSmsCapture(SmsEventBuilder(digest, box), repos.sms),
                    ),
                actionLog = SmsObserverActionLog(repos.actionLogger),
            )
        runBlocking {
            coord.runOneShot(sinceMs = 0L)
            repos.sms.listSince(Instant.EPOCH).size shouldBe 1
        }
    }

    @Test
    fun `runOneShot logs sweep started + stopped`() {
        val coord =
            SmsSweepCoordinator(
                sweeper =
                    SmsContentProviderSweeper(
                        reader = SmsContentProviderReader(context.contentResolver),
                        capture = AutoSmsCapture(SmsEventBuilder(digest, box), repos.sms),
                    ),
                actionLog = SmsObserverActionLog(repos.actionLogger),
            )
        runBlocking {
            coord.runOneShot(sinceMs = 0L)
            val entries = repos.actionLog.recent(10)
            entries.any { it.details["state"] == "running" } shouldBe true
            entries.any { it.details["state"] == "stopped" } shouldBe true
        }
    }
}
