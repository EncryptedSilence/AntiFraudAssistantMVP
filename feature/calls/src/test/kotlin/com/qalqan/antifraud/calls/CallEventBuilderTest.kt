package com.qalqan.antifraud.calls

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.InMemoryCryptoBox
import com.qalqan.antifraud.database.manual.CallEntryDigest
import com.qalqan.antifraud.domain.CallDirection
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class CallEventBuilderTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)
    private val box = InMemoryCryptoBox()
    private val digest = CallEntryDigest.create(context, box)
    private val builder = CallEventBuilder(
        digest = digest,
        contacts = IsKnownContactResolver(repos.contacts),
        repeats = RepeatCallDetector(repos.calls),
    )

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `builds CallEvent from CallLogRow with correct direction and duration`() {
        runBlocking {
            val row = CallLogRow(
                rawNumber = "+71112223344",
                direction = CallLogRow.Direction.INCOMING,
                startedAtMs = 1_700_000_000_000L,
                durationSec = 73L,
                phoneAccountId = null,
            )
            val ev = builder.build(row, simSlot = null)
            ev.direction shouldBe CallDirection.INCOMING
            ev.durationSec shouldBe 73L
            ev.endedAt shouldBe Instant.ofEpochMilli(1_700_000_000_000L).plusSeconds(73L)
            ev.simSlot shouldBe null
            ev.phoneHash.value shouldHaveLength 64 // SHA-256 hex
            ev.isKnownContact shouldBe false
            ev.isRepeated shouldBe false
            ev.callRiskScore shouldBe 0
            ev.linkedSessionId shouldBe null
            ev.linkedCampaignId shouldBe null
        }
    }

    @Test
    fun `MISSED maps to CallDirection_MISSED with zero duration tolerated`() {
        runBlocking {
            val row = CallLogRow("+7111", CallLogRow.Direction.MISSED, 1L, 0L, null)
            builder.build(row, simSlot = null).direction shouldBe CallDirection.MISSED
        }
    }

    @Test
    fun `OUTGOING maps to CallDirection_OUTGOING`() {
        runBlocking {
            val row = CallLogRow("+7111", CallLogRow.Direction.OUTGOING, 1L, 5L, null)
            builder.build(row, simSlot = null).direction shouldBe CallDirection.OUTGOING
        }
    }

    @Test
    fun `UNKNOWN direction maps to incoming as a safe default`() {
        runBlocking {
            val row = CallLogRow("+7111", CallLogRow.Direction.UNKNOWN, 1L, 5L, null)
            builder.build(row, simSlot = null).direction shouldBe CallDirection.INCOMING
        }
    }
}
