package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.RiskEvent

/**
 * Spec §6 — single-condition evaluator. Field lookup goes through the
 * per-event-type `EventFieldAccessor`; operator dispatch handles the six
 * Appendix A operators. Type mismatches and unknown fields return `false`
 * (fault-tolerant — see EventFieldAccessor docs).
 */
object ConditionEvaluator {
    fun evaluate(
        condition: PatternCondition,
        event: RiskEvent,
    ): Boolean =
        accessorFor(condition.eventType)
            ?.lookup(event, condition.field)
            ?.let { actual -> matchOperator(condition.operator, actual, condition.value) }
            ?: false

    private fun matchOperator(
        operator: Operator,
        actual: Any,
        expected: Any,
    ): Boolean =
        when (operator) {
            Operator.EQUALS -> actual == expected
            Operator.IN -> (expected as? List<*>)?.contains(actual) ?: false
            Operator.GREATER_THAN -> compareNumeric(actual, expected) { a, b -> a > b }
            Operator.LESS_THAN -> compareNumeric(actual, expected) { a, b -> a < b }
            Operator.CONTAINS ->
                (actual as? String)
                    ?.let { s -> (expected as? String)?.let { n -> s.contains(n) } ?: false }
                    ?: false
            Operator.MATCHES ->
                (actual as? String)
                    ?.let { s ->
                        (expected as? String)?.let { p ->
                            runCatching { Regex(p).matches(s) }.getOrElse { false }
                        } ?: false
                    }
                    ?: false
        }

    private fun accessorFor(eventType: EventType): EventFieldAccessor? =
        when (eventType) {
            EventType.CALL_EVENT -> CallEventFields
            EventType.SMS_EVENT -> SmsEventFields
            EventType.WEB_EVENT -> WebEventFields
            EventType.USER_ANSWER_EVENT -> UserAnswerFields
            EventType.CONTACT_EVENT, EventType.MANUAL_EVENT, EventType.PATTERN_EVENT -> null
        }

    private inline fun compareNumeric(
        a: Any,
        b: Any,
        op: (Double, Double) -> Boolean,
    ): Boolean =
        (a as? Number)?.toDouble()?.let { ad ->
            (b as? Number)?.toDouble()?.let { bd -> op(ad, bd) }
        } ?: false
}
