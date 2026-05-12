package com.qalqan.antifraud.web

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §5.5.1 — a question prompt may fire ONLY when the campaign score is at high+
 * (i.e. webRiskScore + context score ≥ 60 per §11.5). Below that, no question is asked.
 * Stage 5 records the trigger condition into the action log so Stage 8 / 9 can render
 * the prompt with the §5.5.3 fatigue rules.
 *
 * §23 #18 — the same question is not asked twice in one campaign. Stage 5 does not yet
 * deduplicate by campaign (Stage 9 owns that), so the trigger fires once per high+
 * event; the consuming side filters.
 */
@RunWith(RobolectricTestRunner::class)
class PostSiteQuestionTriggerTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val log = WebObserverActionLog(repos.actionLogger)
    private val trigger = PostSiteQuestionTrigger(log)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `score below HIGH_THRESHOLD does NOT log a question_triggered entry`() {
        runBlocking {
            trigger.maybeRecord(score = 59)
            repos.actionLog.recent(10).none {
                it.details["setting"] == "post_site_question"
            } shouldBe true
        }
    }

    @Test
    fun `score at HIGH_THRESHOLD logs question_triggered`() {
        runBlocking {
            trigger.maybeRecord(score = PostSiteQuestionTrigger.HIGH_THRESHOLD)
            val entry = repos.actionLog.recent(1).single()
            entry.details["setting"] shouldBe "post_site_question"
            entry.details["state"] shouldBe "triggered"
        }
    }

    @Test
    fun `score at CRITICAL_THRESHOLD also logs question_triggered`() {
        runBlocking {
            trigger.maybeRecord(score = 95)
            repos.actionLog.recent(1).single().details["setting"] shouldBe "post_site_question"
        }
    }

    @Test
    fun `score above 100 is treated as critical (clamped semantics)`() {
        runBlocking {
            trigger.maybeRecord(score = 150)
            repos.actionLog.recent(1).single().details["setting"] shouldBe "post_site_question"
        }
    }
}
