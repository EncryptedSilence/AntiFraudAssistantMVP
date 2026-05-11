package com.qalqan.antifraud.calls

import io.kotest.matchers.shouldBe
import org.junit.Test

class PassiveNotificationCopyTest {
    @Test
    fun `body matches §17_0_3 template`() {
        val copy = PassiveNotificationCopy(eventsLast24h = 4, alertsLast24h = 1)
        copy.body shouldBe "Last 24 h: 4 events, 1 alerts."
    }

    @Test
    fun `body handles zero counts`() {
        val copy = PassiveNotificationCopy(eventsLast24h = 0, alertsLast24h = 0)
        copy.body shouldBe "Last 24 h: 0 events, 0 alerts."
    }

    @Test
    fun `title is fixed`() {
        PassiveNotificationCopy(0, 0).title shouldBe "Watching for fraud signals"
    }

    @Test
    fun `negative counts are not allowed`() {
        runCatching { PassiveNotificationCopy(-1, 0) }.isFailure shouldBe true
        runCatching { PassiveNotificationCopy(0, -1) }.isFailure shouldBe true
    }
}
