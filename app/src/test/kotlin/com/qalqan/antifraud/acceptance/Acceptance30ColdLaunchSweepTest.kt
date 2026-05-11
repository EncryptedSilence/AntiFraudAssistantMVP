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
import com.qalqan.antifraud.sms.SmsObserverActionLog
import com.qalqan.antifraud.sms.SmsSweepCoordinator
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
 * while the app process was killed. End-to-end :app integration.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance30ColdLaunchSweepTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()
    private val digest = SmsEntryDigest.create(context, box)

    @After fun tearDown() {
        repos.close()
    }

    @Test
    fun `cold-launch sweep recovers a missed SMS`() {
        val cursor =
            RoboCursor().apply {
                setColumnNames(SmsContentProviderReader.PROJECTION.toList())
                setResults(arrayOf(arrayOf<Any?>("Bank", "Перевод KZT", 1L, 0, 1L)))
            }
        shadowOf(context.contentResolver).setCursor(Telephony.Sms.Inbox.CONTENT_URI, cursor)

        runBlocking {
            repos.sms.listSince(Instant.EPOCH).isEmpty() shouldBe true
            val coord =
                SmsSweepCoordinator(
                    sweeper =
                        SmsContentProviderSweeper(
                            reader = SmsContentProviderReader(context.contentResolver),
                            capture = AutoSmsCapture(SmsEventBuilder(digest, box), repos.sms),
                        ),
                    actionLog = SmsObserverActionLog(repos.actionLogger),
                )
            coord.runOneShot(sinceMs = 0L)
            repos.sms.listSince(Instant.EPOCH).single().senderDisplayNameLocal shouldBe "Bank"
        }
    }
}
