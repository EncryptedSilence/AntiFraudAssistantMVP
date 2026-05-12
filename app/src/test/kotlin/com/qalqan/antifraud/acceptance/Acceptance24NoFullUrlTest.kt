package com.qalqan.antifraud.acceptance

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.WebEntryDigest
import com.qalqan.antifraud.web.DomainNormalizer
import com.qalqan.antifraud.web.DomainSeenChecker
import com.qalqan.antifraud.web.LookalikeDetector
import com.qalqan.antifraud.web.LookalikeSeedCatalog
import com.qalqan.antifraud.web.WebEventBuilder
import com.qalqan.antifraud.web.WebManualCapture
import com.qalqan.antifraud.web.WebObserverActionLog
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

/**
 * Spec §23 #24 / §16.4 / §2.1 — `WebEvent.domainDisplayLocal` is eTLD+1 only. DB
 * inspection across many synthetic inputs confirms no path, query, fragment, scheme,
 * userinfo, port, or whitespace ever lands in the persisted row.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance24NoFullUrlTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val digest = WebEntryDigest.create(context, InMemoryCryptoBox())

    @After fun tearDown() {
        repos.close()
    }

    @Test
    fun `every persisted row has only eTLD plus 1 in domainDisplayLocal`() {
        val inputs =
            listOf(
                "https://halykbank.kz/login?next=x#anchor",
                "http://www.kaspi.kz/profile",
                "https://forte.kz:8443/api?token=secret",
                "https://user:pass@bcc.kz/admin",
                "bereke.kz/path/with/slashes",
                "freedombank.kz?q=1",
            )
        runBlocking {
            val capture =
                WebManualCapture(
                    normalizer = DomainNormalizer(),
                    detector = LookalikeDetector(LookalikeSeedCatalog.seeds),
                    seenChecker = DomainSeenChecker(repos.web),
                    builder = WebEventBuilder(digest),
                    repo = repos.web,
                    actionLog = WebObserverActionLog(repos.actionLogger),
                )
            inputs.forEach { capture.submit(it, Instant.now()) }

            val rows = repos.web.listSince(Instant.EPOCH)
            rows.size shouldBe inputs.size
            rows.forEach { ev ->
                ('/' in ev.domainDisplayLocal) shouldBe false
                ('?' in ev.domainDisplayLocal) shouldBe false
                ('#' in ev.domainDisplayLocal) shouldBe false
                (':' in ev.domainDisplayLocal) shouldBe false
                ('@' in ev.domainDisplayLocal) shouldBe false
                ev.domainDisplayLocal.contains("://") shouldBe false
                ev.domainDisplayLocal.any(Char::isWhitespace) shouldBe false
            }
        }
    }
}
