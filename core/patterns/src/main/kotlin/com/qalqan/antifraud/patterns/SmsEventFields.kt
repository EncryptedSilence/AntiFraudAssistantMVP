package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.RiskEvent

internal object SmsEventFields : EventFieldAccessor {
    override fun lookup(event: RiskEvent, field: String): Any? {
        if (event !is RiskEvent.Sms) return null
        val s = event.event
        return when (field) {
            "containsCode" -> s.containsCode
            "containsLink" -> s.containsLink
            "containsFinancialKeyword" -> s.containsFinancialKeyword
            "containsSecurityKeyword" -> s.containsSecurityKeyword
            "smsCategory" -> s.smsCategory.name
            "simSlot" -> s.simSlot
            else -> null
        }
    }
}
