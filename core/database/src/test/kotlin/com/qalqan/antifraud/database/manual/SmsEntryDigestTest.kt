package com.qalqan.antifraud.database.manual

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class SmsEntryDigestTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()
    private val manual = ManualEntry.create(context, repos, box)
    private val digest = SmsEntryDigest.create(context, box)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `auto-path digest matches manual-path digest for the same sender phone`() {
        runBlocking {
            manual.sms.submit("+71112223344", Instant.now(), "Hello")
            val manualHash = repos.sms.listSince(Instant.EPOCH).first().senderHash.value
            val autoHash = digest.hash("+71112223344").value
            autoHash shouldBe manualHash
        }
    }

    @Test
    fun `auto-path digest matches manual-path digest for a short code`() {
        runBlocking {
            manual.sms.submit("1414", Instant.now(), "Hello")
            val manualHash = repos.sms.listSince(Instant.EPOCH).first().senderHash.value
            val autoHash = digest.hash("1414").value
            autoHash shouldBe manualHash
        }
    }

    @Test
    fun `auto-path digest matches manual-path digest for an alphanumeric sender`() {
        runBlocking {
            manual.sms.submit("HALYKBANK", Instant.now(), "Hello")
            val manualHash = repos.sms.listSince(Instant.EPOCH).first().senderHash.value
            val autoHash = digest.hash("HALYKBANK").value
            autoHash shouldBe manualHash
        }
    }
}
