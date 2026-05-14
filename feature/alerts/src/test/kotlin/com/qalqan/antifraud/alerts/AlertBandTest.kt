package com.qalqan.antifraud.alerts

import com.qalqan.antifraud.domain.RiskBand
import io.kotest.matchers.shouldBe
import org.junit.Test

class AlertBandTest {
    @Test
    fun `LOW maps to SILENT (no notification)`() {
        AlertBand.from(RiskBand.LOW) shouldBe AlertBand.SILENT
    }

    @Test
    fun `MEDIUM maps to REGULAR (non-full-screen)`() {
        AlertBand.from(RiskBand.MEDIUM) shouldBe AlertBand.REGULAR
    }

    @Test
    fun `HIGH maps to FULL_SCREEN`() {
        AlertBand.from(RiskBand.HIGH) shouldBe AlertBand.FULL_SCREEN
    }

    @Test
    fun `CRITICAL maps to FULL_SCREEN_PLUS_OVERLAY`() {
        AlertBand.from(RiskBand.CRITICAL) shouldBe AlertBand.FULL_SCREEN_PLUS_OVERLAY
    }
}
