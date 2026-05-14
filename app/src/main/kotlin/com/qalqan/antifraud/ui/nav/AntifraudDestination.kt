package com.qalqan.antifraud.ui.nav

import androidx.annotation.StringRes
import com.qalqan.antifraud.R

/**
 * Spec §17.1 / §17.2 / §17.3 / §17.4 / §17.6 — top-level destinations.
 *
 * Export, Settings, and Onboarding are reachable but not top-level; they don't appear in
 * the bottom nav. Onboarding routes from MainActivity based on
 * UserSettings.onboardingCompleted (Phase 9). Settings is reached via the Privacy screen.
 */
sealed class AntifraudDestination(
    val route: String,
    @StringRes val labelResId: Int,
) {
    object Home : AntifraudDestination("home", R.string.nav_home)

    object Campaigns : AntifraudDestination("campaigns", R.string.nav_campaigns)

    object Patterns : AntifraudDestination("patterns", R.string.nav_patterns)

    object References : AntifraudDestination("references", R.string.nav_references)

    object Privacy : AntifraudDestination("privacy", R.string.nav_privacy)

    object Onboarding : AntifraudDestination("onboarding", R.string.app_name)

    object Settings : AntifraudDestination("settings", R.string.app_name)

    data class CampaignDetail(val campaignId: String) :
        AntifraudDestination("campaigns/$campaignId", R.string.nav_campaigns) {
        companion object {
            const val ROUTE_PATTERN: String = "campaigns/{campaignId}"
            const val ARG_CAMPAIGN_ID: String = "campaignId"
        }
    }

    companion object {
        fun topLevel(): List<AntifraudDestination> = listOf(Home, Campaigns, Patterns, References, Privacy)
    }
}
