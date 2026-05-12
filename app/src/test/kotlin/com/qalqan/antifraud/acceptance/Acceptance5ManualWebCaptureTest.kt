package com.qalqan.antifraud.acceptance

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.WebEntryDigest
import com.qalqan.antifraud.domain.DomainStatus
import com.qalqan.antifraud.web.DomainNormalizer
import com.qalqan.antifraud.web.DomainSeenChecker
import com.qalqan.antifraud.web.LookalikeDetector
import com.qalqan.antifraud.web.LookalikeSeedCatalog
import com.qalqan.antifraud.web.WebCaptureOutcome
import com.qalqan.antifraud.web.WebEventBuilder
import com.qalqan.antifraud.web.WebManualCapture
import com.qalqan.antifraud.web.WebObserverActionLog
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

/**
 * Spec §23 #5 — manual entry produces a stored `WebEvent` with the expected fields.
 * End-to-end `:app` check exercising the full :feature:web → :core:database path.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance5ManualWebCaptureTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val digest = WebEntryDigest.create(context, InMemoryCryptoBox())

    @After fun tearDown() {
        repos.close()
    }

    @Test
    fun `https URL with path persists as eTLD plus 1 only with expected fields`() {
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
            val visitedAt = Instant.parse("2026-05-12T10:00:00Z")
            val r = capture.submit("https://www.HalykBank.kz/login?next=x#a", visitedAt)
            r.shouldBeInstanceOf<WebCaptureOutcome.Saved>()

            val saved = repos.web.find(r.id)!!
            saved.domainDisplayLocal shouldBe "halykbank.kz"
            saved.visitedAt shouldBe visitedAt
            saved.isNewDomain shouldBe true
            saved.domainStatus shouldBe DomainStatus.NEW
            saved.webRiskScore shouldBe 0
        }
    }
}
