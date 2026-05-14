package com.qalqan.antifraud.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.scoring.Sensitivity
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserSettingsTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val settings = UserSettings(context)

    @After
    fun tearDown() {
        context.getSharedPreferences("antifraud_user_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun `sensitivity defaults to STANDARD per spec §18`() {
        settings.sensitivity shouldBe Sensitivity.STANDARD
    }

    @Test
    fun `sensitivity round-trips through SharedPreferences`() {
        settings.sensitivity = Sensitivity.HIGH
        UserSettings(context).sensitivity shouldBe Sensitivity.HIGH
    }

    @Test
    fun `sensitivity write tolerates an unknown stored value by returning STANDARD`() {
        context.getSharedPreferences("antifraud_user_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("sensitivity", "unknown_value")
            .commit()
        UserSettings(context).sensitivity shouldBe Sensitivity.STANDARD
    }
}
