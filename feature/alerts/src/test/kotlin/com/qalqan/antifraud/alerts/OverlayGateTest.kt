package com.qalqan.antifraud.alerts

import io.kotest.matchers.shouldBe
import org.junit.Test

class OverlayGateTest {
    @Test
    fun `denied permission blocks overlay`() {
        OverlayGate.shouldFire(
            canDrawOverlays = false,
            band = AlertBand.FULL_SCREEN_PLUS_OVERLAY,
            foregroundIsRelevant = true,
        ) shouldBe false
    }

    @Test
    fun `non-critical band blocks overlay`() {
        OverlayGate.shouldFire(
            canDrawOverlays = true,
            band = AlertBand.FULL_SCREEN,
            foregroundIsRelevant = true,
        ) shouldBe false
    }

    @Test
    fun `irrelevant foreground app blocks overlay`() {
        OverlayGate.shouldFire(
            canDrawOverlays = true,
            band = AlertBand.FULL_SCREEN_PLUS_OVERLAY,
            foregroundIsRelevant = false,
        ) shouldBe false
    }

    @Test
    fun `all three conditions satisfied fires overlay`() {
        OverlayGate.shouldFire(
            canDrawOverlays = true,
            band = AlertBand.FULL_SCREEN_PLUS_OVERLAY,
            foregroundIsRelevant = true,
        ) shouldBe true
    }
}
