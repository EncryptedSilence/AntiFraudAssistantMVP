package com.qalqan.antifraud.ui.nav

import com.qalqan.antifraud.R
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test

class AntifraudDestinationTest {
    @Test
    fun `top-level destinations match the v2 shell ordering`() {
        AntifraudDestination.topLevel().map { it.route } shouldBe
            listOf("home", "activity", "add", "lists", "profile")
    }

    @Test
    fun `each top-level destination has a label and an icon`() {
        AntifraudDestination.topLevel().forEach { dest ->
            dest.labelResId shouldBe
                when (dest.route) {
                    "home" -> R.string.nav_overview
                    "activity" -> R.string.nav_activity
                    "add" -> R.string.nav_add
                    "lists" -> R.string.nav_lists
                    "profile" -> R.string.nav_profile
                    else -> error("unexpected route: ${dest.route}")
                }
            dest.icon shouldNotBe null
        }
    }
}
