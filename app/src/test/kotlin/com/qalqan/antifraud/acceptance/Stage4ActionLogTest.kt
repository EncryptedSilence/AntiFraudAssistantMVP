package com.qalqan.antifraud.acceptance

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import com.qalqan.antifraud.sms.SmsObserverActionLog
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec §20.1 — the action log records SMS permission grants/denials and sweep state
 * changes WITHOUT carrying sender, body, or any user-typed text. The forbidden-detail-key
 * invariant is enforced by `ApplicationActionLogEntry.init`.
 */
@RunWith(RobolectricTestRunner::class)
class Stage4ActionLogTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)

    @After fun tearDown() {
        repos.close()
    }

    @Test
    fun `sweep started, permission grant, and sweep stopped produce three entries`() {
        val log = SmsObserverActionLog(repos.actionLogger)
        runBlocking {
            log.sweepStarted()
            log.grant(android.Manifest.permission.RECEIVE_SMS)
            log.sweepStopped()

            val entries = repos.actionLog.recent(10)
            entries.size shouldBe 3
            entries.map { it.action } shouldContain AppAction.SETTING_CHANGED
            entries.map { it.action } shouldContain AppAction.PERMISSION_GRANTED
            // Forbidden keys are rejected at constructor time; we assert the keys present
            // don't include any sender / body / URL / OTP key.
            entries.flatMap { it.details.keys }.forEach { k ->
                val forbidden =
                    setOf(
                        "sender", "senderId", "smsBody", "body", "messageBody",
                        "phoneNumber", "phone", "phoneNormalized", "url", "domain",
                        "otp", "code", "userNote", "userText",
                    )
                (k in forbidden) shouldBe false
            }
        }
    }
}
