package com.qalqan.antifraud.patterns

/**
 * Spec Appendix A — seven event types referenceable in pattern conditions.
 *
 * Stage 2 ships interpreters for the four event types Stage 1 produces data for:
 * CallEvent, SmsEvent, WebEvent, UserAnswerEvent. Patterns referencing the
 * other three are rejected at parse time so they cannot be silently ignored.
 */
enum class EventType(val jsonValue: String, val supportedInStage2: Boolean) {
    CALL_EVENT("CallEvent", true),
    SMS_EVENT("SmsEvent", true),
    WEB_EVENT("WebEvent", true),
    USER_ANSWER_EVENT("UserAnswerEvent", true),
    CONTACT_EVENT("ContactEvent", false),
    MANUAL_EVENT("ManualEvent", false),
    PATTERN_EVENT("PatternEvent", false),
    ;

    companion object {
        fun fromJson(value: String): EventType? = entries.firstOrNull { it.jsonValue == value }
    }
}
