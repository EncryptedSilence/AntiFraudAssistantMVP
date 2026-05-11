package com.qalqan.antifraud.sms

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

class SmsBroadcastTest {
    @Test
    fun `holds sender, body, receivedAt, simSlot`() {
        val now = Instant.now()
        val b = SmsBroadcast(
            rawSender = "1414",
            body = "Hello",
            receivedAt = now,
            simSlot = 0,
        )
        b.rawSender shouldBe "1414"
        b.body shouldBe "Hello"
        b.receivedAt shouldBe now
        b.simSlot shouldBe 0
    }

    @Test
    fun `simSlot may be null on legacy devices`() {
        SmsBroadcast("S", "B", Instant.now(), simSlot = null).simSlot shouldBe null
    }
}
