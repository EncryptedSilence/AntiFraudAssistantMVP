package com.qalqan.antifraud.alerts

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import org.junit.Test

class AlertPermissionRequesterTest {
    @Test
    fun `each Stage 9 permission has a non-empty justification`() {
        AlertPermissionRequester.justifications.keys shouldBe
            setOf(
                "android.permission.USE_FULL_SCREEN_INTENT",
                "android.permission.SYSTEM_ALERT_WINDOW",
            )
        AlertPermissionRequester.justifications.values.forEach { it.shouldNotBeEmpty() }
    }

    @Test
    fun `justifications are user-readable plain language (no permission constant in copy)`() {
        AlertPermissionRequester.justifications.forEach { (_, copy) ->
            copy.contains("android.permission.") shouldBe false
            copy.contains("permission.") shouldBe false
        }
    }
}
