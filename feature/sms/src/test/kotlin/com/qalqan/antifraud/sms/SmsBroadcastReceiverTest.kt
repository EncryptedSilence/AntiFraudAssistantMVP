package com.qalqan.antifraud.sms

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.SmsEntryDigest
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class SmsBroadcastReceiverTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private lateinit var repos: Repositories
    private lateinit var box: InMemoryCryptoBox
    private lateinit var digest: SmsEntryDigest

    @Before
    fun setUp() {
        repos = Repositories.inMemory(context)
        box = InMemoryCryptoBox()
        digest = SmsEntryDigest.create(context, box)
        SmsBroadcastReceiver.captureProvider = { _ ->
            SmsBroadcastReceiver.CaptureHandle(
                capture =
                    AutoSmsCapture(
                        builder = SmsEventBuilder(digest = digest, box = box),
                        sms = repos.sms,
                    ),
                close = { /* repos is closed in tearDown */ },
            )
        }
    }

    @After
    fun tearDown() {
        repos.close()
        SmsBroadcastReceiver.captureProvider = SmsBroadcastReceiver.defaultCaptureProvider
    }

    @Test
    fun `onSmsReceived persists an SmsEvent through the injected capture`() {
        val receiver = SmsBroadcastReceiver()
        runBlocking {
            receiver.onSmsReceived(
                context,
                SmsBroadcast("HALYKBANK", "Перевод 5000 KZT", Instant.now(), null),
            )
            val saved = repos.sms.listSince(Instant.EPOCH).single()
            saved.senderDisplayNameLocal shouldBe "HALYKBANK"
        }
    }

    @Test
    fun `onSmsReceived swallows captureProvider failures gracefully`() {
        SmsBroadcastReceiver.captureProvider = { _ ->
            throw SecurityException("simulated KeyStore failure")
        }
        val receiver = SmsBroadcastReceiver()
        runBlocking {
            receiver.onSmsReceived(
                context,
                SmsBroadcast("S", "B", Instant.now(), null),
            )
            repos.sms.listSince(Instant.EPOCH).isEmpty() shouldBe true
        }
    }
}
