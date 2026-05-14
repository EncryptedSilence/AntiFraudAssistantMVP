package com.qalqan.antifraud.ui.nav

import com.qalqan.antifraud.R
import io.kotest.matchers.shouldBe
import org.junit.Test

class AntifraudDestinationTest {
    @Test
    fun `top-level destinations match spec §17 ordering`() {
        AntifraudDestination.topLevel().map { it.route } shouldBe
            listOf("home", "campaigns", "patterns", "references", "privacy")
    }

    @Test
    fun `each destination has a label and an icon-content-description string-resource id`() {
        AntifraudDestination.topLevel().forEach { dest ->
            dest.labelResId shouldBe
                when (dest.route) {
                    "home" -> R.string.nav_home
                    "campaigns" -> R.string.nav_campaigns
                    "patterns" -> R.string.nav_patterns
                    "references" -> R.string.nav_references
                    "privacy" -> R.string.nav_privacy
                    else -> error("unexpected route: ${dest.route}")
                }
        }
    }
}
