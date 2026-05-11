package com.qalqan.antifraud.calls

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class RiskCounterUpdaterTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val updater = RiskCounterUpdater(repos.contacts)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `unknown caller creates a profile with riskCounter starting at 1`() {
        runBlocking {
            updater.bump(sampleCall("h_unknown", isKnown = false))
            val profile = repos.contacts.findByHash(PhoneHash("h_unknown"))!!
            profile.riskCounter shouldBe 1
            profile.isInContacts shouldBe false
        }
    }

    @Test
    fun `repeated unknown calls increment riskCounter`() {
        runBlocking {
            updater.bump(sampleCall("h_repeat", isKnown = false))
            updater.bump(sampleCall("h_repeat", isKnown = false))
            updater.bump(sampleCall("h_repeat", isKnown = false))
            repos.contacts.findByHash(PhoneHash("h_repeat"))!!.riskCounter shouldBe 3
        }
    }

    @Test
    fun `known caller is a no-op (the user already trusts them)`() {
        runBlocking {
            // Pre-seed a known profile.
            repos.contacts.save(
                com.qalqan.antifraud.domain.ContactProfile(
                    id = UUID.randomUUID().toString(),
                    phoneNormalizedEnc = ByteArray(0),
                    phoneHash = PhoneHash("h_known"),
                    phoneLast4 = "1234",
                    isShortCode = false,
                    displayNameLocal = null,
                    isInContacts = true,
                    trustStatus = com.qalqan.antifraud.domain.TrustStatus.NEUTRAL,
                    firstSeenAt = Instant.now(),
                    lastSeenAt = Instant.now(),
                    riskCounter = 0,
                    userComment = null,
                ),
            )
            updater.bump(sampleCall("h_known", isKnown = true))
            repos.contacts.findByHash(PhoneHash("h_known"))!!.riskCounter shouldBe 0
        }
    }

    private fun sampleCall(hash: String, isKnown: Boolean): CallEvent =
        CallEvent(
            id = EventId(UUID.randomUUID().toString()),
            phoneHash = PhoneHash(hash),
            simSlot = null,
            direction = CallDirection.INCOMING,
            startedAt = Instant.now(),
            endedAt = Instant.now().plusSeconds(30),
            durationSec = 30,
            isKnownContact = isKnown,
            isRepeated = false,
            callRiskScore = 0,
            linkedSessionId = null,
            linkedCampaignId = null,
        )
}
