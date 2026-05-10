package com.qalqan.antifraud.acceptance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.SenderHash
import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.domain.SmsEvent
import com.qalqan.antifraud.patterns.BatchPatternMatcher
import com.qalqan.antifraud.patterns.SeedPatternLoader
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

/**
 * Spec §23 #14 — a disabled pattern does not contribute to scoring.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DisabledPatternAcceptanceTest {
    private val ctx: Context = ApplicationProvider.getApplicationContext()
    private val repos = Repositories.inMemory(ctx)
    private val anchor = Instant.parse("2026-05-08T10:00:00Z")

    @After
    fun close() = repos.close()

    @Test
    fun `disabled bank_security pattern does not trigger even when events match (spec §23 #14)`() {
        runBlocking {
            val events: List<RiskEvent> =
                listOf(
                    RiskEvent.Call(
                        CallEvent(
                            id = EventId("call-1"),
                            phoneHash = PhoneHash("h-unknown"),
                            simSlot = null,
                            direction = CallDirection.INCOMING,
                            startedAt = anchor,
                            endedAt = anchor.plusSeconds(240),
                            durationSec = 240,
                            isKnownContact = false,
                            isRepeated = false,
                            callRiskScore = 0,
                            linkedSessionId = null,
                            linkedCampaignId = null,
                        ),
                    ),
                    RiskEvent.Sms(
                        SmsEvent(
                            id = EventId("sms-1"),
                            senderHash = SenderHash("sender-hash"),
                            senderDisplayNameLocal = "BANK24",
                            simSlot = null,
                            receivedAt = anchor.plusSeconds(360),
                            smsCategory = SmsCategory.OTP,
                            containsCode = true,
                            containsLink = false,
                            containsFinancialKeyword = false,
                            containsSecurityKeyword = false,
                            bodyExcerptEnc = ByteArray(0),
                            smsRiskScore = 0,
                            linkedSessionId = null,
                            linkedCampaignId = null,
                        ),
                    ),
                )

            repos.patternState.setEnabled(
                patternId = "bank_security_otp_after_call_v1",
                enabled = false,
                at = anchor,
            )

            val patterns =
                SeedPatternLoader.load().map { p ->
                    val isEnabled = repos.patternState.isEnabled(p.patternId.value, default = p.enabled)
                    p.copy(enabled = isEnabled)
                }

            val results = BatchPatternMatcher.matchAll(patterns, events)

            val bankSecurity = results.first { it.patternId.value == "bank_security_otp_after_call_v1" }
            bankSecurity.matched.shouldBeFalse()
            bankSecurity.triggeredWeight shouldBe 0
        }
    }
}
