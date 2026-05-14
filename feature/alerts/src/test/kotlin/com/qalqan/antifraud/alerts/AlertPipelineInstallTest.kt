package com.qalqan.antifraud.alerts

import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Smoke-only: this test verifies that the AlertPipeline class is instantiable in the
 * `:feature:alerts` test classpath. The actual wiring test lives in `:app` (T33) because
 * the install code references `:feature:calls.CallObserverService`.
 */
class AlertPipelineInstallTest {
    @Test
    fun `AlertPipeline class exists`() {
        AlertPipeline::class.simpleName shouldBe "AlertPipeline"
    }
}
