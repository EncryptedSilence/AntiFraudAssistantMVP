package com.qalqan.antifraud.database.manual

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.domain.CallDirection
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class CallEntryDigestTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()
    private val manual = ManualEntry.create(context, repos, box)
    private val digest = CallEntryDigest.create(context, box)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `auto-path digest matches manual-path digest for the same phone`() {
        runBlocking {
            manual.calls.submit("+71112223344", CallDirection.INCOMING, Instant.now(), 10, false)
            val manualHash = repos.calls.listSince(Instant.EPOCH).first().phoneHash.value
            val autoHash = digest.hash("+71112223344").value
            autoHash shouldBe manualHash
        }
    }

    @Test
    fun `auto-path digest normalizes Kazakh local-format numbers the same way as manual`() {
        runBlocking {
            manual.calls.submit("87112223344", CallDirection.INCOMING, Instant.now(), 10, false)
            val manualHash = repos.calls.listSince(Instant.EPOCH).first().phoneHash.value
            val autoHash = digest.hash("87112223344").value
            autoHash shouldBe manualHash
        }
    }
}
