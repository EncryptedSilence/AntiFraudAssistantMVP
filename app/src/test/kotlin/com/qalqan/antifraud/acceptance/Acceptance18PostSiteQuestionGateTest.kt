package com.qalqan.antifraud.acceptance

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.web.PostSiteQuestionTrigger
import com.qalqan.antifraud.web.WebObserverActionLog
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §23 #18 / §5.5.1 — post-site questions appear at high+ risk only. Below the
 * threshold no question is triggered. End-to-end `:app` check on the trigger writer.
 *
 * The actual prompt UI (Stage 8 / 9) reads the action-log trigger entry and applies
 * the §5.5.3 fatigue rules ("same question not asked twice in one campaign"). This
 * acceptance test pins the §5.5.1 floor; the fatigue rule is Stage 8 / 9's contract.
 */
@RunWith(RobolectricTestRunner::class)
class Acceptance18PostSiteQuestionGateTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val log = WebObserverActionLog(repos.actionLogger)
    private val trigger = PostSiteQuestionTrigger(log)

    @After fun tearDown() {
        repos.close()
    }

    @Test
    fun `no question is triggered at low risk (score 10)`() {
        runBlocking {
            trigger.maybeRecord(score = 10)
            repos.actionLog.recent(5).none {
                it.details["setting"] == "post_site_question"
            } shouldBe true
        }
    }

    @Test
    fun `no question is triggered at medium risk (score 45)`() {
        runBlocking {
            trigger.maybeRecord(score = 45)
            repos.actionLog.recent(5).none {
                it.details["setting"] == "post_site_question"
            } shouldBe true
        }
    }

    @Test
    fun `question IS triggered at high risk (score 65)`() {
        runBlocking {
            trigger.maybeRecord(score = 65)
            val entry = repos.actionLog.recent(1).single()
            entry.details["setting"] shouldBe "post_site_question"
            entry.details["state"] shouldBe "triggered"
        }
    }

    @Test
    fun `question IS triggered at critical risk (score 85)`() {
        runBlocking {
            trigger.maybeRecord(score = 85)
            repos.actionLog.recent(1).single().details["setting"] shouldBe "post_site_question"
        }
    }
}
