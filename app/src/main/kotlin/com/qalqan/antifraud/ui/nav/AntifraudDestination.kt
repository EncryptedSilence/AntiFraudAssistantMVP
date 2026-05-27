package com.qalqan.antifraud.ui.nav

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.qalqan.antifraud.R

/**
 * Spec §17 + docs/plans/ui_v2 — top-level destinations of the v2 shell.
 *
 * The five top-level tabs are Overview / Activity / Add / Lists / Profile. The remaining
 * screens (Campaigns, Patterns, References, Privacy, Settings) are reachable but not
 * top-level; they are reached from the Profile tab or via drill-in. Onboarding routes from
 * MainActivity based on UserSettings.onboardingCompleted.
 *
 * Note: the Overview route id stays "home" so the existing HomeHost wiring and the bottom-bar
 * back-stack anchor keep working unchanged.
 */
sealed class AntifraudDestination(
    val route: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector? = null,
) {
    object Home : AntifraudDestination("home", R.string.nav_overview, Icons.Filled.Home)

    object Activity : AntifraudDestination("activity", R.string.nav_activity, Icons.Filled.Notifications)

    object Add : AntifraudDestination("add", R.string.nav_add, Icons.Filled.Add)

    object Lists : AntifraudDestination("lists", R.string.nav_lists, Icons.AutoMirrored.Filled.List)

    object Profile : AntifraudDestination("profile", R.string.nav_profile, Icons.Filled.Person)

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
        fun topLevel(): List<AntifraudDestination> = listOf(Home, Activity, Add, Lists, Profile)
    }
}
