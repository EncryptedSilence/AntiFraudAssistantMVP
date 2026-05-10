package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.RiskEvent

/**
 * Resolves a `PatternCondition.field` string to a typed value on a domain event.
 * Returns `null` when the field is unknown for this event type.
 *
 * Spec §6.4: "The interpreter must reject any unknown operator or field." We
 * surface unknown fields here as `null`; the calling `ConditionEvaluator`
 * treats `null` as a non-match rather than throwing — pattern interpretation
 * is fault-tolerant by design.
 */
internal interface EventFieldAccessor {
    fun lookup(
        event: RiskEvent,
        field: String,
    ): Any?
}
