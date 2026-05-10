package com.qalqan.antifraud.patterns

/**
 * Spec Appendix A `conditions[]` item — `field` is opaque here; per-event-type field
 * resolution happens in `EventFieldAccessor` in Phase 4. Weight ∈ [0, 100],
 * `timeWindowHours` ∈ [1, 336] (= 14 days) when present.
 *
 * Stage 2 only ships interpreters for CallEvent, SmsEvent, WebEvent, UserAnswerEvent;
 * conditions referencing other event types are rejected here so a malformed pattern
 * cannot reach the matcher.
 */
data class PatternCondition(
    val eventType: EventType,
    val field: String,
    val operator: Operator,
    val value: Any,
    val weight: Int,
    val timeWindowHours: Int? = null
) {
    init {
        require(eventType.supportedInStage2) {
            "condition references event type ${eventType.jsonValue} not supported in Stage 2"
        }
        require(field.isNotBlank()) { "field must not be blank" }
        require(weight in 0..MAX_WEIGHT) { "weight must be in 0..$MAX_WEIGHT" }
        timeWindowHours?.let {
            require(it in 1..MAX_TIME_WINDOW_HOURS) {
                "timeWindowHours must be in 1..$MAX_TIME_WINDOW_HOURS when present"
            }
        }
    }

    companion object {
        const val MAX_WEIGHT: Int = 100
        const val MAX_TIME_WINDOW_HOURS: Int = 336
    }
}
