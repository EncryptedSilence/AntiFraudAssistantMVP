package com.qalqan.antifraud.database.manual

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

/**
 * Spec §5.1 / §16.4 — auto-capture and manual-entry must produce byte-identical
 * `domainHash` values for the same canonical eTLD+1. The salt-sharing path is the same
 * one already used for [CallEntryDigest] and [SmsEntryDigest].
 */
@RunWith(RobolectricTestRunner::class)
class WebEntryDigestTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()

    @After
    fun tearDown() { repos.close() }

    @Test
    fun `digest produces the same hash that ManualEntry WebSubmitter writes`() {
        val manual = ManualEntry.create(context, repos, box)
        val digest = WebEntryDigest.create(context, box)

        runBlocking {
            val id = manual.web.submit(domainEtldPlusOne = "halykbank.kz", visitedAt = Instant.now())
            val saved = repos.web.find(id)!!
            digest.hash("halykbank.kz") shouldBe saved.domainHash.value
        }
    }

    @Test
    fun `digest is deterministic across instances given the same context+box`() {
        val a = WebEntryDigest.create(context, box)
        val b = WebEntryDigest.create(context, box)
        a.hash("kaspi.kz") shouldBe b.hash("kaspi.kz")
    }
}
