package com.qalqan.antifraud.scoring

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ContextRiskTest {
    @Test fun `SMS arrived after a call adds 25`() {
        ContextRisk.compute(setOf(LinkSignal.CALL_AFTER_SMS)) shouldBe 25
        ContextRisk.compute(setOf(LinkSignal.SMS_AFTER_CALL)) shouldBe 25
    }

    @Test fun `site after call or SMS adds 25`() {
        ContextRisk.compute(setOf(LinkSignal.SITE_AFTER_CALL_OR_SMS)) shouldBe 25
    }

    @Test fun `multiple unknowns within 24h adds 20`() {
        ContextRisk.compute(setOf(LinkSignal.MULTIPLE_UNKNOWN_24H)) shouldBe 20
    }

    @Test fun `signals stack but cap at 60`() {
        ContextRisk.compute(
            setOf(
                LinkSignal.SMS_AFTER_CALL,
                LinkSignal.CALL_AFTER_SMS,
                LinkSignal.SITE_AFTER_CALL_OR_SMS,
                LinkSignal.MULTIPLE_UNKNOWN_24H,
            ),
        ) shouldBe 60
    }

    @Test fun `temporal only and weak signals contribute zero`() {
        ContextRisk.compute(setOf(LinkSignal.TEMPORAL_ONLY, LinkSignal.WEAK)) shouldBe 0
    }
}
