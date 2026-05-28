package com.qalqan.antifraud.ui.campaign

import android.content.Context
import com.qalqan.antifraud.R
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.patterns.EventType
import com.qalqan.antifraud.patterns.PatternCondition
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * App-layer presentation helpers for the §17.2 Campaign screens.
 *
 * Pattern names and per-condition reasons are localized here rather than in `:core:patterns`:
 * the core `ConditionPhraser` is deliberately locale-neutral English (§14), and the spec lets
 * the §17 UI substitute localized phrasings. This keeps the alert pipeline (which also consumes
 * the English `Reason.text`) untouched.
 */
private val INSTANT_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault())

/** Renders an [Instant] as a local "dd.MM.yyyy HH:mm" string instead of a raw ISO-8601 toString(). */
fun formatInstant(instant: Instant): String = INSTANT_FORMATTER.format(instant)

/** Localized display name for the five in-APK seed patterns; bundle patterns fall back to [fallback]. */
fun patternNameFor(
    context: Context,
    patternId: String,
    fallback: String,
): String =
    when (patternId) {
        "bank_security_otp_after_call_v1" -> context.getString(R.string.pattern_name_bank_security_otp)
        "unknown_call_then_link_sms_v1" -> context.getString(R.string.pattern_name_unknown_call_link_sms)
        "authority_spoof_call_v1" -> context.getString(R.string.pattern_name_authority_spoof_call)
        "new_lookalike_domain_visit_v1" -> context.getString(R.string.pattern_name_new_lookalike_domain)
        "multistage_pressure_campaign_v1" -> context.getString(R.string.pattern_name_multistage_pressure)
        else -> fallback
    }

/** Localized event-type label for a linked [RiskEvent] in the §17.2 detail timeline. */
fun eventTypeLabel(
    context: Context,
    event: RiskEvent,
): String =
    context.getString(
        when (event) {
            is RiskEvent.Call -> R.string.event_type_call
            is RiskEvent.Sms -> R.string.event_type_sms
            is RiskEvent.Web -> R.string.event_type_web
            is RiskEvent.Answer -> R.string.event_type_answer
        },
    )

/** Localized per-condition reason, mirroring the §14 English `ConditionPhraser` in :core:patterns. */
fun localizedReason(
    context: Context,
    condition: PatternCondition,
): String =
    context.getString(
        when (condition.eventType) {
            EventType.CALL_EVENT -> callReason(condition)
            EventType.SMS_EVENT -> smsReason(condition)
            EventType.WEB_EVENT -> webReason(condition)
            EventType.USER_ANSWER_EVENT -> answerReason(condition)
            else -> R.string.reason_generic
        },
    )

private fun callReason(c: PatternCondition): Int =
    when (c.field) {
        "isKnownContact" -> if (c.value == false) R.string.reason_call_unknown else R.string.reason_call_known
        "isRepeated" -> R.string.reason_call_repeated
        "direction" -> R.string.reason_call_direction
        "durationSec" -> R.string.reason_call_duration
        else -> R.string.reason_generic_call
    }

private fun smsReason(c: PatternCondition): Int =
    when (c.field) {
        "containsCode" -> if (c.value == true) R.string.reason_sms_code else R.string.reason_generic_sms
        "containsLink" -> if (c.value == true) R.string.reason_sms_link else R.string.reason_generic_sms
        "containsFinancialKeyword" -> R.string.reason_sms_financial
        "containsSecurityKeyword" -> R.string.reason_sms_security
        "smsCategory" -> R.string.reason_sms_category
        else -> R.string.reason_generic_sms
    }

private fun webReason(c: PatternCondition): Int =
    when (c.field) {
        "isNewDomain" -> if (c.value == true) R.string.reason_web_new else R.string.reason_generic_web
        "domainStatus" -> R.string.reason_web_status
        "domainDisplayLocal" -> R.string.reason_web_lookalike
        "webRiskScore" -> R.string.reason_web_risk
        else -> R.string.reason_generic_web
    }

private fun answerReason(c: PatternCondition): Int =
    when (c.field) {
        "questionCode" ->
            when (c.value) {
                "Q1_CALLER_OFFICIAL_CLAIM" -> R.string.reason_answer_official_claim
                "Q3_ASKED_TO_ACT_NOW" -> R.string.reason_answer_act_now
                else -> R.string.reason_generic_answer
            }
        "answerCode" -> R.string.reason_answer_confirmed
        else -> R.string.reason_generic_answer
    }
