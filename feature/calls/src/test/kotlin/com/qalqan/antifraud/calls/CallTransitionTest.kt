package com.qalqan.antifraud.calls

import io.kotest.matchers.shouldBe
import org.junit.Test

class CallTransitionTest {
    @Test
    fun `transition state encodes ringing offhook idle`() {
        CallTransition.State.RINGING.id shouldBe "RINGING"
        CallTransition.State.OFFHOOK.id shouldBe "OFFHOOK"
        CallTransition.State.IDLE.id shouldBe "IDLE"
    }
}
