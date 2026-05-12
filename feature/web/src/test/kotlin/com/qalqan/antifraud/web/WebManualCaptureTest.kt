package com.qalqan.antifraud.web

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.WebEntryDigest
import com.qalqan.antifraud.domain.DomainStatus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class WebManualCaptureTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val digest = WebEntryDigest.create(context, InMemoryCryptoBox())

    @After
    fun tearDown() {
        repos.close()
    }

    private fun newCapture(): WebManualCapture =
        WebManualCapture(
            normalizer = DomainNormalizer(),
            detector = LookalikeDetector(LookalikeSeedCatalog.seeds),
            seenChecker = DomainSeenChecker(repos.web),
            builder = WebEventBuilder(digest),
            repo = repos.web,
            actionLog = WebObserverActionLog(repos.actionLogger),
        )

    @Test
    fun `URL with scheme + path lands as eTLD plus 1 only`() {
        runBlocking {
            val r =
                newCapture().submit(
                    rawInput = "https://www.HalykBank.kz/login?next=1#a",
                    visitedAt = Instant.parse("2026-05-12T10:00:00Z"),
                )
            r.shouldBeInstanceOf<WebCaptureOutcome.Saved>()
            r.canonical shouldBe "halykbank.kz"

            val saved = repos.web.find(r.id)!!
            saved.domainDisplayLocal shouldBe "halykbank.kz"
            saved.isNewDomain shouldBe true
            saved.domainStatus shouldBe DomainStatus.NEW
        }
    }

    @Test
    fun `revisit flips isNewDomain to false and status to KNOWN`() {
        runBlocking {
            val capture = newCapture()
            val first = capture.submit("kaspi.kz", Instant.parse("2026-05-12T10:00:00Z"))
            first.shouldBeInstanceOf<WebCaptureOutcome.Saved>()
            val second = capture.submit("kaspi.kz", Instant.parse("2026-05-12T10:05:00Z"))
            second.shouldBeInstanceOf<WebCaptureOutcome.Saved>()
            second.isNewDomain shouldBe false
            second.status shouldBe DomainStatus.KNOWN
        }
    }

    @Test
    fun `lookalike of a known bank lands as SUSPICIOUS`() {
        runBlocking {
            val r =
                newCapture().submit(
                    rawInput = "halykbamk.kz",
                    visitedAt = Instant.parse("2026-05-12T10:00:00Z"),
                )
            r.shouldBeInstanceOf<WebCaptureOutcome.Saved>()
            r.lookalike?.seed shouldBe "halykbank.kz"
            r.status shouldBe DomainStatus.SUSPICIOUS
        }
    }

    @Test
    fun `empty input is rejected without DB write`() {
        runBlocking {
            val r = newCapture().submit(rawInput = "  ", visitedAt = Instant.now())
            r shouldBe WebCaptureOutcome.Rejected.Empty
            repos.web.listSince(Instant.EPOCH).isEmpty() shouldBe true
        }
    }

    @Test
    fun `invalid input is rejected without DB write`() {
        runBlocking {
            val r = newCapture().submit(rawInput = "localhost", visitedAt = Instant.now())
            r.shouldBeInstanceOf<WebCaptureOutcome.Rejected.Invalid>()
            repos.web.listSince(Instant.EPOCH).isEmpty() shouldBe true
        }
    }

    @Test
    fun `§23 #5 — manual entry produces a stored WebEvent with the expected fields`() {
        runBlocking {
            val visitedAt = Instant.parse("2026-05-12T10:00:00Z")
            val r = newCapture().submit("https://kaspi.kz/profile?x=1", visitedAt)
            r.shouldBeInstanceOf<WebCaptureOutcome.Saved>()

            val saved = repos.web.find(r.id)!!
            saved.domainDisplayLocal shouldBe "kaspi.kz"
            saved.visitedAt shouldBe visitedAt
            saved.isNewDomain shouldBe true
            saved.domainStatus shouldBe DomainStatus.NEW
            saved.webRiskScore shouldBe 0
            saved.linkedSessionId shouldBe null
            saved.linkedCampaignId shouldBe null
            // §16.4 / §23 #24 boundary — re-check at the DB read path.
            ('/' in saved.domainDisplayLocal) shouldBe false
            ('?' in saved.domainDisplayLocal) shouldBe false
            ('#' in saved.domainDisplayLocal) shouldBe false
            saved.domainDisplayLocal.contains("://") shouldBe false
        }
    }

    @Test
    fun `submit logs manual_site_submitted exactly once and NOT lookalike when no match`() {
        runBlocking {
            newCapture().submit("kaspi.kz", Instant.now())
            val entries = repos.actionLog.recent(5)
            entries.count { it.details["setting"] == "manual_site_submitted" } shouldBe 1
            entries.none { it.details["setting"] == "lookalike_match" } shouldBe true
        }
    }

    @Test
    fun `submit logs both manual_site_submitted and lookalike_match for a typo`() {
        runBlocking {
            newCapture().submit("halykbamk.kz", Instant.now())
            val entries = repos.actionLog.recent(5)
            entries.count { it.details["setting"] == "manual_site_submitted" } shouldBe 1
            entries.count { it.details["setting"] == "lookalike_match" } shouldBe 1
        }
    }
}
