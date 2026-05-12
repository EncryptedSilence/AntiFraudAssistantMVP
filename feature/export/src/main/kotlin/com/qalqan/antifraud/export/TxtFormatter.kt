package com.qalqan.antifraud.export

/**
 * Spec §8.5 — TXT formatter. Plain text with one record per blank-line-separated block,
 * grouped under `=== CATEGORY_NAME ===` headers. Deterministic: records are stable-sorted
 * within each category, fields appear in a fixed schema-defined order.
 *
 * Encoding: UTF-8 (no BOM). Line ending: LF. The byte output is what the preview path
 * displays in the Compose `<pre>` block and what the writer streams to disk — same bytes.
 */
internal object TxtFormatter : ExportFormatter {
    override val format = ExportFormat.TXT

    override fun format(
        records: List<ExportRecord>,
        request: ExportRequest,
    ): ByteArray {
        val sb = StringBuilder()
        val byCategory = records.groupBy { it.category }
        request.categories.forEach { category ->
            sb.append("=== ").append(category.name).append(" ===\n")
            val sorted = sortStable(byCategory[category].orEmpty())
            sorted.forEach { record ->
                appendRecord(sb, record)
                sb.append('\n')
            }
        }
        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    private fun sortStable(records: List<ExportRecord>): List<ExportRecord> = records.sortedBy { stableKey(it) }

    private fun stableKey(record: ExportRecord): String =
        when (record) {
            is ExportRecord.SuspiciousNumber -> record.phoneLast4
            is ExportRecord.RiskCampaign -> record.campaignId
            is ExportRecord.TriggeredPattern -> record.patternId
        }

    private fun appendRecord(
        sb: StringBuilder,
        record: ExportRecord,
    ) {
        when (record) {
            is ExportRecord.SuspiciousNumber -> {
                sb.append("phoneFull: ").append(record.phoneFull.orEmpty()).append('\n')
                sb.append("phoneLast4: ").append(record.phoneLast4).append('\n')
                sb.append("isShortCode: ").append(record.isShortCode).append('\n')
                sb.append("displayName: ").append(record.displayName.orEmpty()).append('\n')
                sb.append("trustStatus: ").append(record.trustStatus).append('\n')
                sb.append("firstSeenAt: ").append(record.firstSeenAt).append('\n')
                sb.append("riskCounter: ").append(record.riskCounter).append('\n')
            }
            is ExportRecord.RiskCampaign -> {
                sb.append("campaignId: ").append(record.campaignId).append('\n')
                sb.append("startedAt: ").append(record.startedAt).append('\n')
                sb.append("lastEventAt: ").append(record.lastEventAt).append('\n')
                sb.append("status: ").append(record.status).append('\n')
                sb.append("scenarioType: ").append(record.scenarioType).append('\n')
                sb.append("campaignRiskScore: ").append(record.campaignRiskScore).append('\n')
                sb.append("campaignRiskLevel: ").append(record.campaignRiskLevel).append('\n')
                sb.append("relatedEventCount: ").append(record.relatedEventCount).append('\n')
                sb.append("explanation: ").append(record.explanation).append('\n')
            }
            is ExportRecord.TriggeredPattern -> {
                sb.append("patternId: ").append(record.patternId).append('\n')
                sb.append("name: ").append(record.name).append('\n')
                sb.append("category: ").append(record.scenarioCategory).append('\n')
                sb.append("version: ").append(record.version).append('\n')
                sb.append("triggeredAt: ").append(record.triggeredAt).append('\n')
                sb.append("timesTriggered: ").append(record.timesTriggered).append('\n')
            }
        }
    }
}
