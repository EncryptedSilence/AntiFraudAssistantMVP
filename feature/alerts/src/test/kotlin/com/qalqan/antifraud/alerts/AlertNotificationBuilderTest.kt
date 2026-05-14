package com.qalqan.antifraud.alerts

import android.app.Notification
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotContain
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AlertNotificationBuilderTest {
    private val ctx: Context get() = ApplicationProvider.getApplicationContext()
    private val builder = AlertNotificationBuilder()
    private val content =
        AlertContent(
            reasons =
                listOf(
                    "Caller is not in your contacts.",
                    "Call lasted over a minute.",
                    "Risk level reached critical.",
                ),
        )

    @Test
    fun `critical notification sets CATEGORY_CALL`() {
        AlertChannels.ensure(ctx)
        val n = builder.build(ctx, content = content, band = AlertBand.FULL_SCREEN_PLUS_OVERLAY)
        n.category shouldBe Notification.CATEGORY_CALL
    }

    @Test
    fun `critical and high notifications carry a full-screen intent`() {
        AlertChannels.ensure(ctx)
        builder.build(ctx, content, AlertBand.FULL_SCREEN_PLUS_OVERLAY).fullScreenIntent shouldNotBe null
        builder.build(ctx, content, AlertBand.FULL_SCREEN).fullScreenIntent shouldNotBe null
    }

    @Test
    fun `medium notification has no full-screen intent`() {
        AlertChannels.ensure(ctx)
        builder.build(ctx, content, AlertBand.REGULAR).fullScreenIntent shouldBe null
    }

    @Test
    fun `notification content never contains forbidden patterns`() {
        AlertChannels.ensure(ctx)
        val n = builder.build(ctx, content, AlertBand.FULL_SCREEN_PLUS_OVERLAY)
        val text =
            "${n.extras.getString(Notification.EXTRA_TITLE)} " +
                "${n.extras.getString(Notification.EXTRA_TEXT)} " +
                n.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.joinToString(" ").orEmpty()
        text shouldNotContain "+7"
        text shouldNotContain "kaspi"
        text shouldNotContain ".kz"
    }
}
