package com.qalqan.antifraud.scoring

import com.qalqan.antifraud.domain.AnswerCode
import com.qalqan.antifraud.domain.AnswerId
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.QuestionCode
import com.qalqan.antifraud.domain.UserAnswer
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.random.Random

class EventRiskInvariantTest {
    private val seedT = Instant.parse("2026-05-08T10:00:00Z")

    @Test
    fun `EventRisk for calls never exceeds 100 across 1000 random inputs`() {
        val rng = Random(424242)
        repeat(1_000) {
            val durationSec = rng.nextLong(0, 3_600)
            val isKnownContact = rng.nextBoolean()
            val isRepeated = rng.nextBoolean()
            val call = CallEvent(
                id = EventId("e$it"),
                phoneHash = PhoneHash("h"),
                simSlot = null,
                direction = CallDirection.INCOMING,
                startedAt = seedT,
                endedAt = seedT.plusSeconds(durationSec),
                durationSec = durationSec,
                isKnownContact = isKnownContact,
                isRepeated = isRepeated,
                callRiskScore = 0,
                linkedSessionId = null,
                linkedCampaignId = null
            )
            val signals = LinkSignal.entries.filter { _ -> rng.nextBoolean() }.toSet()
            val answers = (0..rng.nextInt(0, 4)).map { i ->
                UserAnswer(
                    id = AnswerId("a$it-$i"),
                    relatedEventId = call.id,
                    relatedSessionId = null,
                    relatedCampaignId = null,
                    questionCode = QuestionCode.entries.random(rng),
                    answerCode = AnswerCode.entries.random(rng),
                    userNoteLocalEnc = null,
                    answerRiskScore = 0,
                    createdAt = seedT
                )
            }
            EventRiskCalculator.computeForCall(call, signals, answers) shouldBeLessThanOrEqual 100
        }
    }
}
