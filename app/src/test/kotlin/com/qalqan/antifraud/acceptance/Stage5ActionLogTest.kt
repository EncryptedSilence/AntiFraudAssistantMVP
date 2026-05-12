package com.qalqan.antifraud.acceptance

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.WebEntryDigest
import com.qalqan.antifraud.web.DomainNormalizer
import com.qalqan.antifraud.web.DomainSeenChecker
import com.qalqan.antifraud.web.LookalikeDetector
import com.qalqan.antifraud.web.LookalikeSeedCatalog
import com.qalqan.antifraud.web.PostSiteQuestionTrigger
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
 * Spec §20.1 / §2.1 — running the full Stage 5 path (submit a lookalike + trigger a
 * question) writes only state markers to the action log, NEVER the domain, URL, typed
 * input, or seed string.
 */
@RunWith(RobolectricTestRunner::class)
class Stage5ActionLogTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val digest = WebEntryDigest.create(context, InMemoryCryptoBox())

    @After fun tearDown() {
        repos.close()
    }

    @Test
    fun `lookalike + high-score path writes setting+state markers without forbidden keys`() {
        runBlocking {
            val log = WebObserverActionLog(repos.actionLogger)
            val capture =
                WebManualCapture(
                    normalizer = DomainNormalizer(),
                    detector = LookalikeDetector(LookalikeSeedCatalog.seeds),
                    seenChecker = DomainSeenChecker(repos.web),
                    builder = WebEventBuilder(digest),
                    repo = repos.web,
                    actionLog = log,
                )
            // 1-edit-distance lookalike of halykbank.kz.
            capture.submit("halykbamk.kz", Instant.now())
            PostSiteQuestionTrigger(log).maybeRecord(score = 80)

            val entries = repos.actionLog.recent(10)
            entries.map { it.details["setting"] } shouldBe
                listOf(
                    "post_site_question",
                    "lookalike_match",
                    "manual_site_submitted",
                )
            val forbidden =
                setOf(
                    "domain", "domainHash", "url", "seed", "input",
                    "phone", "phoneNumber", "phoneNormalized",
                    "sender", "senderId", "smsBody", "body", "messageBody",
                    "otp", "code", "userNote", "userText",
                )
            entries.flatMap { it.details.keys }.forEach { k ->
                (k in forbidden) shouldBe false
            }
        }
    }
}
