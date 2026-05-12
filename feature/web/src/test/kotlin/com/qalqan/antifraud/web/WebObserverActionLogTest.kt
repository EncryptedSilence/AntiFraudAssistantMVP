package com.qalqan.antifraud.web

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WebObserverActionLogTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val log = WebObserverActionLog(repos.actionLogger)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `manualSubmitted records SETTING_CHANGED with state=recorded`() {
        runBlocking {
            log.manualSubmitted()
            val entry = repos.actionLog.recent(1).single()
            entry.action shouldBe AppAction.SETTING_CHANGED
            entry.details["setting"] shouldBe "manual_site_submitted"
            entry.details["state"] shouldBe "recorded"
            // §20.1 — no domain, url, seed, or any user input value
            ("domain" in entry.details) shouldBe false
            ("url" in entry.details) shouldBe false
            ("seed" in entry.details) shouldBe false
            ("input" in entry.details) shouldBe false
        }
    }

    @Test
    fun `lookalikeTriggered records distance but NOT the seed string`() {
        runBlocking {
            log.lookalikeTriggered(distance = 1)
            val entry = repos.actionLog.recent(1).single()
            entry.action shouldBe AppAction.SETTING_CHANGED
            entry.details["setting"] shouldBe "lookalike_match"
            entry.details["distance"] shouldBe "1"
            ("seed" in entry.details) shouldBe false
            ("domain" in entry.details) shouldBe false
        }
    }
}
