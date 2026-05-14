package com.qalqan.antifraud.alerts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class OnboardingPermissionStepsTest {
    private val ctx: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `pre-14 without overlay permission returns SystemAlertWindow step`() {
        // Robolectric default for canDrawOverlays at SDK 33 is false until the
        // appop is granted, so the gate fires the overlay step.
        val step = OnboardingPermissionSteps.nextRequiredStep(ctx)
        step.shouldBeInstanceOf<OnboardingPermissionSteps.Step.SystemAlertWindow>()
        step.intent shouldNotBe null
    }
}
