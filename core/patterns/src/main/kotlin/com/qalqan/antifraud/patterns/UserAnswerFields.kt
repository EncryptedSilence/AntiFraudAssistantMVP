package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.RiskEvent

internal object UserAnswerFields : EventFieldAccessor {
    override fun lookup(event: RiskEvent, field: String): Any? {
        if (event !is RiskEvent.Answer) return null
        val a = event.event
        return when (field) {
            "questionCode" -> a.questionCode.name
            "answerCode" -> a.answerCode.name
            else -> null
        }
    }
}
