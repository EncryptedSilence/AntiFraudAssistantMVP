package com.qalqan.antifraud.calls

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.ManualEntry
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.PhoneHash
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class RepeatCallDetectorTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val manual = ManualEntry.create(context, repos, InMemoryCryptoBox())

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `no prior calls means not repeated`() {
        runBlocking {
            val detector = RepeatCallDetector(repos.calls)
            detector.isRepeated(PhoneHash("hashA"), Instant.now()) shouldBe false
        }
    }

    @Test
    fun `prior call within 24h means repeated`() {
        runBlocking {
            val now = Instant.now()
            manual.calls.submit("+71112223344", CallDirection.INCOMING, now.minusSeconds(60), 30, false)
            val priorHash = repos.calls.listSince(Instant.EPOCH).first().phoneHash
            RepeatCallDetector(repos.calls).isRepeated(priorHash, now) shouldBe true
        }
    }

    @Test
    fun `prior call older than 24h does not count as repeated`() {
        runBlocking {
            val now = Instant.now()
            manual.calls.submit("+71112223344", CallDirection.INCOMING, now.minusSeconds(36 * 3600), 30, false)
            val priorHash = repos.calls.listSince(Instant.EPOCH).first().phoneHash
            RepeatCallDetector(repos.calls).isRepeated(priorHash, now) shouldBe false
        }
    }
}
