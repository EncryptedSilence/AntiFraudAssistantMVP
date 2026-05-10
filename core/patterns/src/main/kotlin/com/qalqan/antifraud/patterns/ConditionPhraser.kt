package com.qalqan.antifraud.patterns

/**
 * Per-condition human-readable phrasing for §14 reasons.
 *
 * Phrases are deliberately terse and locale-neutral English. The §17 UI may
 * substitute localized phrasings post-Stage-2; this object is the single source
 * of truth for what each (eventType, field, operator, value) tuple means.
 */
internal object ConditionPhraser {
    fun phrase(condition: PatternCondition): String = when (condition.eventType) {
        EventType.CALL_EVENT -> phraseCall(condition)
        EventType.SMS_EVENT -> phraseSms(condition)
        EventType.WEB_EVENT -> phraseWeb(condition)
        EventType.USER_ANSWER_EVENT -> phraseAnswer(condition)
        EventType.CONTACT_EVENT -> "Contact event condition '${condition.field}' matched."
        EventType.MANUAL_EVENT -> "Manual event condition '${condition.field}' matched."
        EventType.PATTERN_EVENT -> "Pattern event condition '${condition.field}' matched."
    }

    private fun phraseCall(c: PatternCondition): String = when (c.field) {
        "isKnownContact" -> if (c.value == false) "An unknown call was received." else "A known contact called."
        "isRepeated" -> "Repeat call from the same number."
        "direction" -> "Call direction matched ${c.value}."
        "durationSec" -> "Call duration condition matched (${c.operator.jsonValue} ${c.value})."
        else -> "Call event condition '${c.field}' matched."
    }

    private fun phraseSms(c: PatternCondition): String = when (c.field) {
        "containsCode" ->
            if (c.value == true) "An SMS with a verification code arrived." else "SMS without code condition matched."
        "containsLink" ->
            if (c.value == true) "An SMS with a link arrived." else "SMS without link condition matched."
        "containsFinancialKeyword" -> "SMS with financial keyword arrived."
        "containsSecurityKeyword" -> "SMS with security keyword arrived."
        "smsCategory" -> "SMS category matched ${c.value}."
        else -> "SMS event condition '${c.field}' matched."
    }

    private fun phraseWeb(c: PatternCondition): String = when (c.field) {
        "isNewDomain" -> if (c.value == true) "A new domain was visited." else "Visited a known domain."
        "domainStatus" -> "Domain status matched ${c.value}."
        "domainDisplayLocal" -> "Domain ${c.operator.jsonValue} '${c.value}'."
        "webRiskScore" -> "Web risk score ${c.operator.jsonValue} ${c.value}."
        else -> "Web event condition '${c.field}' matched."
    }

    private fun phraseAnswer(c: PatternCondition): String = when (c.field) {
        "questionCode" -> "User answered question ${c.value}."
        "answerCode" -> "User's answer was ${c.value}."
        else -> "User answer condition '${c.field}' matched."
    }
}
