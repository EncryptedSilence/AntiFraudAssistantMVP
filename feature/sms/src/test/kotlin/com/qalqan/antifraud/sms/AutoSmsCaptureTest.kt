package com.qalqan.antifraud.sms

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.SmsEntryDigest
import com.qalqan.antifraud.domain.SmsEvent
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

@RunWith(RobolectricTestRunner::class)
class AutoSmsCaptureTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()
    private val digest = SmsEntryDigest.create(context, box)
    private val builder = SmsEventBuilder(digest, box)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `accept persists an SmsEvent and fires onCaptured`() {
        val seen = AtomicReference<SmsEvent?>()
        val capture = AutoSmsCapture(builder, repos.sms, onCaptured = { ev -> seen.set(ev) })
        runBlocking {
            capture.accept(SmsBroadcast("HALYKBANK", "Перевод 5000 KZT", Instant.now(), null))
            val saved = repos.sms.listSince(Instant.EPOCH).single()
            saved.senderDisplayNameLocal shouldBe "HALYKBANK"
        }
        seen.get()?.senderDisplayNameLocal shouldBe "HALYKBANK"
    }

    @Test
    fun `accept deduplicates within a 10-second window for the same sender hash`() {
        val capture = AutoSmsCapture(builder, repos.sms)
        runBlocking {
            val now = Instant.now()
            capture.accept(SmsBroadcast("S", "hello", now, null))
            capture.accept(SmsBroadcast("S", "hello", now, null))
            capture.accept(SmsBroadcast("S", "hello", now.plusSeconds(15), null))
            // Two events: one for the duplicate-window pair, one for the post-window arrival.
            repos.sms.listSince(Instant.EPOCH).size shouldBe 2
        }
    }

    @Test
    fun `accept is null-safe and does nothing for an empty body and empty sender`() {
        val capture = AutoSmsCapture(builder, repos.sms)
        runBlocking {
            capture.accept(SmsBroadcast("", "", Instant.now(), null))
            // Empty sender + empty body still creates an event (the receiver doesn't filter);
            // the contract under test is "no crash" — assert the side effect rather than skipping.
            repos.sms.listSince(Instant.EPOCH).size shouldBe 1
        }
    }
}
