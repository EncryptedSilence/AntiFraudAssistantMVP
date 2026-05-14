package com.qalqan.antifraud.settings

import android.content.Context
import com.qalqan.antifraud.scoring.Sensitivity
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Spec §18 — persistent user-settings storage.
 *
 * Backed by SharedPreferences named `antifraud_user_prefs`. All reads are synchronous and
 * cheap (in-memory after the first access); writes use `apply()` (async to disk) so caller
 * threads never block. Mirrors the [com.qalqan.antifraud.sync.SyncSettings] pattern from
 * Stage 6.
 */
class UserSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var sensitivity: Sensitivity
        get() {
            val raw = prefs.getString(KEY_SENSITIVITY, null) ?: return Sensitivity.STANDARD
            return Sensitivity.entries.firstOrNull { it.name == raw } ?: Sensitivity.STANDARD
        }
        set(value) {
            prefs.edit().putString(KEY_SENSITIVITY, value.name).apply()
        }

    var callAnalysisEnabled: Boolean by booleanPref(KEY_CALL_ANALYSIS, default = true)
    var smsAnalysisEnabled: Boolean by booleanPref(KEY_SMS_ANALYSIS, default = true)
    var webAnalysisEnabled: Boolean by booleanPref(KEY_WEB_ANALYSIS, default = true)
    var riskCampaignsEnabled: Boolean by booleanPref(KEY_RISK_CAMPAIGNS, default = true)
    var localPatternsEnabled: Boolean by booleanPref(KEY_LOCAL_PATTERNS, default = true)
    var referenceSyncEnabled: Boolean by booleanPref(KEY_REFERENCE_SYNC, default = true)
    var patternSyncEnabled: Boolean by booleanPref(KEY_PATTERN_SYNC, default = true)
    var notificationsEnabled: Boolean by booleanPref(KEY_NOTIFICATIONS, default = true)
    var postCallQuestionsEnabled: Boolean by booleanPref(KEY_POST_CALL_Q, default = true)
    var postSmsQuestionsEnabled: Boolean by booleanPref(KEY_POST_SMS_Q, default = true)
    var postSiteQuestionsEnabled: Boolean by booleanPref(KEY_POST_SITE_Q, default = true)
    var automaticArchivingEnabled: Boolean by booleanPref(KEY_AUTO_ARCHIVING, default = true)
    var advancedRulesEnabled: Boolean by booleanPref(KEY_ADVANCED_RULES, default = false)
    var educationalCardsEnabled: Boolean by booleanPref(KEY_EDU_CARDS, default = true)
    var onboardingCompleted: Boolean by booleanPref(KEY_ONBOARDING_DONE, default = false)
    var lastEducationalCardAtMs: Long
        get() = prefs.getLong(KEY_LAST_EDU_AT, 0L)
        set(value) {
            prefs.edit().putLong(KEY_LAST_EDU_AT, value).apply()
        }

    private fun booleanPref(
        key: String,
        default: Boolean,
    ) = object : ReadWriteProperty<Any?, Boolean> {
        override fun getValue(
            thisRef: Any?,
            property: KProperty<*>,
        ): Boolean = prefs.getBoolean(key, default)

        override fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: Boolean,
        ) {
            prefs.edit().putBoolean(key, value).apply()
        }
    }

    companion object {
        const val PREFS_NAME: String = "antifraud_user_prefs"
        private const val KEY_SENSITIVITY: String = "sensitivity"
        private const val KEY_CALL_ANALYSIS: String = "call_analysis"
        private const val KEY_SMS_ANALYSIS: String = "sms_analysis"
        private const val KEY_WEB_ANALYSIS: String = "web_analysis"
        private const val KEY_RISK_CAMPAIGNS: String = "risk_campaigns"
        private const val KEY_LOCAL_PATTERNS: String = "local_patterns"
        private const val KEY_REFERENCE_SYNC: String = "reference_sync"
        private const val KEY_PATTERN_SYNC: String = "pattern_sync"
        private const val KEY_NOTIFICATIONS: String = "notifications"
        private const val KEY_POST_CALL_Q: String = "post_call_q"
        private const val KEY_POST_SMS_Q: String = "post_sms_q"
        private const val KEY_POST_SITE_Q: String = "post_site_q"
        private const val KEY_AUTO_ARCHIVING: String = "auto_archiving"
        private const val KEY_ADVANCED_RULES: String = "advanced_rules"
        private const val KEY_EDU_CARDS: String = "edu_cards"
        private const val KEY_ONBOARDING_DONE: String = "onboarding_done"
        private const val KEY_LAST_EDU_AT: String = "last_edu_at_ms"
    }
}
