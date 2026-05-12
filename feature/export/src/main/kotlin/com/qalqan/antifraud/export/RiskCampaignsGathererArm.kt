package com.qalqan.antifraud.export

import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.CampaignStatus
import com.qalqan.antifraud.domain.RiskCampaign

/**
 * Spec §8.2 — emits a [ExportRecord.RiskCampaign] for every campaign with
 * `status ∈ {ACTIVE, CLOSED}`. Archived and false-positive campaigns are intentionally
 * excluded — they represent user-dismissed risk and should not surface in a
 * deliberately-shared export.
 *
 * Domain → export-string conversions: `CampaignStatus` and `ScenarioCategory` are
 * emitted as their lowercase enum names; `RiskBand` as its lowercase name; `null`
 * `scenarioType` and `explanation` are coerced to `"unknown"` / `""`.
 */
internal object RiskCampaignsGathererArm : GathererArm {
    override suspend fun gather(repositories: Repositories): List<ExportRecord> {
        val all = repositories.campaigns.listAll()
        return all
            .filter { it.status == CampaignStatus.ACTIVE || it.status == CampaignStatus.CLOSED }
            .map { it.toExportRecord() }
    }

    private fun RiskCampaign.toExportRecord(): ExportRecord.RiskCampaign =
        ExportRecord.RiskCampaign(
            campaignId = campaignId.value,
            startedAt = startedAt,
            lastEventAt = lastEventAt,
            status = status.name.lowercase(),
            scenarioType = scenarioType?.name?.lowercase() ?: "unknown",
            campaignRiskScore = campaignRiskScore,
            campaignRiskLevel = campaignRiskBand.name.lowercase(),
            relatedEventCount = relatedEventIds.size,
            explanation = explanation.orEmpty(),
        )
}
