package com.qalqan.antifraud.patterns

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class EventTypeTest {
    @Test
    fun `event types match Appendix A enum values`() {
        EventType.entries.map { it.jsonValue }.toSet() shouldBe
            setOf(
                "CallEvent", "SmsEvent", "WebEvent",
                "ContactEvent", "UserAnswerEvent", "ManualEvent", "PatternEvent",
            )
    }

    @Test
    fun `supported event types are the four Stage 2 ships`() {
        EventType.entries.filter { it.supportedInStage2 }.map { it.jsonValue }.toSet() shouldBe
            setOf(
                "CallEvent", "SmsEvent", "WebEvent", "UserAnswerEvent",
            )
    }

    @Test
    fun `fromJson resolves a known event type`() {
        EventType.fromJson("CallEvent") shouldBe EventType.CALL_EVENT
        EventType.fromJson("UserAnswerEvent") shouldBe EventType.USER_ANSWER_EVENT
    }

    @Test
    fun `fromJson returns null for an unknown event type`() {
        EventType.fromJson("VoiceEvent") shouldBe null
    }
}
