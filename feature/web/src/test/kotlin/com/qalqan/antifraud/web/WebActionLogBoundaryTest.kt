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
 * Spec §20.1 / §2.1 — the action log NEVER stores: a domain, a URL, a typed input,
 * the matched seed, an OTP, an ID number, or a phone number. The constructor-time
 * invariant on `ApplicationActionLogEntry` already enforces this; we re-check at the
 * Stage-5 boundary so a regression here surfaces before integration.
 */
@RunWith(RobolectricTestRunner::class)
class WebActionLogBoundaryTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val log = WebObserverActionLog(repos.actionLogger)

    @After
    fun tearDown() { repos.close() }

    @Test
    fun `Stage 5 action-log entries never carry forbidden detail keys`() {
        runBlocking {
            log.manualSubmitted()
            log.lookalikeTriggered(distance = 2)
            log.questionTriggered()

            val entries = repos.actionLog.recent(10)
            entries.size shouldBe 3
            val forbidden = setOf(
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
