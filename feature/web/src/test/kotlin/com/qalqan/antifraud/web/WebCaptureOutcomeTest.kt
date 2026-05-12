package com.qalqan.antifraud.web

import com.qalqan.antifraud.domain.DomainStatus
import com.qalqan.antifraud.domain.EventId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test

class WebCaptureOutcomeTest {
    @Test
    fun `Saved carries the persisted event id and the orchestrator-decided signals`() {
        val o =
            WebCaptureOutcome.Saved(
                id = EventId("00000000-0000-0000-0000-000000000001"),
                canonical = "kaspi.kz",
                status = DomainStatus.NEW,
                isNewDomain = true,
                lookalike = null,
            )
        o.id.value shouldBe "00000000-0000-0000-0000-000000000001"
        o.canonical shouldBe "kaspi.kz"
        o.status shouldBe DomainStatus.NEW
        o.isNewDomain shouldBe true
        o.lookalike shouldBe null
    }

    @Test
    fun `Rejected_Empty and Rejected_Invalid are distinguishable`() {
        WebCaptureOutcome.Rejected.Empty.shouldBeInstanceOf<WebCaptureOutcome.Rejected>()
        WebCaptureOutcome.Rejected.Invalid("garbage").shouldBeInstanceOf<WebCaptureOutcome.Rejected>()
        WebCaptureOutcome.Rejected.Invalid("garbage").input shouldBe "garbage"
    }
}
