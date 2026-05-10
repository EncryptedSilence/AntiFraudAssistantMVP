package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.RiskEvent

internal object WebEventFields : EventFieldAccessor {
    override fun lookup(event: RiskEvent, field: String): Any? {
        if (event !is RiskEvent.Web) return null
        val w = event.event
        return when (field) {
            "domainDisplayLocal" -> w.domainDisplayLocal
            "isNewDomain" -> w.isNewDomain
            "domainStatus" -> w.domainStatus.name
            "webRiskScore" -> w.webRiskScore
            else -> null
        }
    }
}
