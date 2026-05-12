package com.qalqan.antifraud.web

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.ManualEntry
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class DomainSeenCheckerTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val manual = ManualEntry.create(context, repos, InMemoryCryptoBox())
    private val checker = DomainSeenChecker(repos.web)

    @After
    fun tearDown() { repos.close() }

    @Test
    fun `unseen domain hash returns isNew = true`() {
        runBlocking {
            checker.isNew(hashHex = "0".repeat(SHA256_HEX_LEN)) shouldBe true
        }
    }

    @Test
    fun `seen domain hash returns isNew = false`() {
        runBlocking {
            val id = manual.web.submit(domainEtldPlusOne = "halykbank.kz", visitedAt = Instant.now())
            val saved = repos.web.find(id)!!
            checker.isNew(saved.domainHash.value) shouldBe false
        }
    }

    private companion object {
        const val SHA256_HEX_LEN = 64
    }
}
