package com.qalqan.antifraud.ui.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.scoring.Sensitivity
import com.qalqan.antifraud.settings.UserSettings
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Spec Appendix B + §18 — pin that UserSettings.sensitivity round-trips through the
 * Stage-1 scoring math. The actual orchestrator wiring lands when Stage 9 needs it; here
 * we just pin that the math is unchanged.
 */
@RunWith(RobolectricTestRunner::class)
class SensitivityAppliedToScoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun tearDown() {
        context.getSharedPreferences("antifraud_user_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun `Sensitivity_HIGH scales a base score of 50 to 60 per Appendix B`() {
        UserSettings(context).sensitivity = Sensitivity.HIGH
        UserSettings(context).sensitivity.applyTo(50) shouldBe 60
    }

    @Test
    fun `Sensitivity_MAXIMUM caps at 100`() {
        UserSettings(context).sensitivity = Sensitivity.MAXIMUM
        UserSettings(context).sensitivity.applyTo(80) shouldBe 100
    }
}
