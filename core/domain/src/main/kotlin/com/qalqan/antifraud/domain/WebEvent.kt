package com.qalqan.antifraud.domain

import java.time.Instant

/**
 * Spec §16.4 — eTLD+1 only, never path/query/cookie/content (§2.1).
 */
data class WebEvent(
    val id: EventId,
    val domainHash: DomainHash,
    val domainDisplayLocal: String,
    val visitedAt: Instant,
    val isNewDomain: Boolean,
    val domainStatus: DomainStatus,
    val webRiskScore: Int,
    val linkedSessionId: SessionId?,
    val linkedCampaignId: CampaignId?
) {
    init {
        require(domainDisplayLocal.isNotBlank()) { "domainDisplayLocal must not be blank" }
        require('/' !in domainDisplayLocal) { "domainDisplayLocal must not contain '/'" }
        require('?' !in domainDisplayLocal) { "domainDisplayLocal must not contain '?'" }
        require('#' !in domainDisplayLocal) { "domainDisplayLocal must not contain '#'" }
        require(!domainDisplayLocal.contains("://")) {
            "domainDisplayLocal must not contain a protocol scheme"
        }
        require(webRiskScore in 0..100) { "webRiskScore must be in 0..100" }
    }
}
