package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.RiskEvent

/**
 * Spec §6 — single-condition evaluator. Field lookup goes through the
 * per-event-type `EventFieldAccessor`; operator dispatch handles the six
 * Appendix A operators. Type mismatches and unknown fields return `false`
 * (fault-tolerant — see EventFieldAccessor docs).
 */
object ConditionEvaluator {
    fun evaluate(condition: PatternCondition, event: RiskEvent): Boolean {
        val accessor = accessorFor(condition.eventType) ?: return false
        val actual = accessor.lookup(event, condition.field) ?: return false
        return when (condition.operator) {
            Operator.EQUALS -> actual == condition.value
            Operator.IN -> {
                val list = condition.value as? List<*> ?: return false
                list.contains(actual)
            }
            Operator.GREATER_THAN -> compareNumeric(actual, condition.value) { a, b -> a > b }
            Operator.LESS_THAN -> compareNumeric(actual, condition.value) { a, b -> a < b }
            Operator.CONTAINS -> {
                val haystack = actual as? String ?: return false
                val needle = condition.value as? String ?: return false
                haystack.contains(needle)
            }
            Operator.MATCHES -> {
                val haystack = actual as? String ?: return false
                val pattern = condition.value as? String ?: return false
                runCatching { Regex(pattern).matches(haystack) }.getOrElse { false }
            }
        }
    }

    private fun accessorFor(eventType: EventType): EventFieldAccessor? = when (eventType) {
        EventType.CALL_EVENT -> CallEventFields
        EventType.SMS_EVENT -> SmsEventFields
        EventType.WEB_EVENT -> WebEventFields
        EventType.USER_ANSWER_EVENT -> UserAnswerFields
        EventType.CONTACT_EVENT, EventType.MANUAL_EVENT, EventType.PATTERN_EVENT -> null
    }

    private inline fun compareNumeric(a: Any, b: Any, op: (Double, Double) -> Boolean): Boolean {
        val ad = (a as? Number)?.toDouble() ?: return false
        val bd = (b as? Number)?.toDouble() ?: return false
        return op(ad, bd)
    }
}
