package com.qalqan.antifraud.alerts

import io.kotest.matchers.shouldBe
import org.junit.Test

class RelevantForegroundAppDetectorTest {
    private val detector = RelevantForegroundAppDetector()

    @Test
    fun `kaspi banking package is relevant`() {
        detector.isRelevant("kz.kaspi.mobile") shouldBe true
    }

    @Test
    fun `halyk banking package is relevant`() {
        detector.isRelevant("kz.halykbank.halyk") shouldBe true
    }

    @Test
    fun `chrome browser is relevant`() {
        detector.isRelevant("com.android.chrome") shouldBe true
    }

    @Test
    fun `unrelated package is not relevant`() {
        detector.isRelevant("com.spotify.music") shouldBe false
    }

    @Test
    fun `null package is not relevant (no foreground info available)`() {
        detector.isRelevant(null) shouldBe false
    }
}
