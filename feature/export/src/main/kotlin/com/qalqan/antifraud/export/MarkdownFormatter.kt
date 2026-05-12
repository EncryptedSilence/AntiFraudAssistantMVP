package com.qalqan.antifraud.export

/**
 * Spec §8.5 — Markdown formatter. Each category gets an H2 heading and a pipe-separated
 * table. Pipe characters in field values are escaped as `\|`. UTF-8, LF. Deterministic
 * stable-sorted within each category.
 */
internal object MarkdownFormatter : ExportFormatter {
    override val format = ExportFormat.MARKDOWN

    override fun format(
        records: List<ExportRecord>,
        request: ExportRequest,
    ): ByteArray {
        val sb = StringBuilder()
        val byCategory = records.groupBy { it.category }
        request.categories.forEach { category ->
            sb.append("## ").append(category.name).append("\n\n")
            val sorted = sortStable(byCategory[category].orEmpty())
            when (category) {
                ExportCategory.SUSPICIOUS_NUMBERS -> writeSuspiciousNumbers(sb, sorted)
                ExportCategory.RISK_CAMPAIGNS -> writeRiskCampaigns(sb, sorted)
                ExportCategory.TRIGGERED_PATTERNS -> writeTriggeredPatterns(sb, sorted)
            }
            sb.append('\n')
        }
        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    @Suppress("MaxLineLength")
    private fun writeSuspiciousNumbers(
        sb: StringBuilder,
        records: List<ExportRecord>,
    ) {
        sb.append("| phoneFull | phoneLast4 | isShortCode | displayName | trustStatus | firstSeenAt | riskCounter |\n")
        sb.append("| --- | --- | --- | --- | --- | --- | --- |\n")
        records.filterIsInstance<ExportRecord.SuspiciousNumber>().forEach { r ->
            sb.append("| ").append(esc(r.phoneFull.orEmpty()))
                .append(" | ").append(esc(r.phoneLast4))
                .append(" | ").append(r.isShortCode)
                .append(" | ").append(esc(r.displayName.orEmpty()))
                .append(" | ").append(esc(r.trustStatus))
                .append(" | ").append(r.firstSeenAt)
                .append(" | ").append(r.riskCounter)
                .append(" |\n")
        }
    }

    private fun writeRiskCampaigns(
        sb: StringBuilder,
        records: List<ExportRecord>,
    ) {
        sb.append("| campaignId | startedAt | lastEventAt | status | scenarioType")
            .append(" | campaignRiskScore | campaignRiskLevel | relatedEventCount | explanation |\n")
        sb.append("| --- | --- | --- | --- | --- | --- | --- | --- | --- |\n")
        records.filterIsInstance<ExportRecord.RiskCampaign>().forEach { r ->
            sb.append("| ").append(esc(r.campaignId))
                .append(" | ").append(r.startedAt)
                .append(" | ").append(r.lastEventAt)
                .append(" | ").append(esc(r.status))
                .append(" | ").append(esc(r.scenarioType))
                .append(" | ").append(r.campaignRiskScore)
                .append(" | ").append(esc(r.campaignRiskLevel))
                .append(" | ").append(r.relatedEventCount)
                .append(" | ").append(esc(r.explanation))
                .append(" |\n")
        }
    }

    private fun writeTriggeredPatterns(
        sb: StringBuilder,
        records: List<ExportRecord>,
    ) {
        sb.append("| patternId | name | category | version | triggeredAt | timesTriggered |\n")
        sb.append("| --- | --- | --- | --- | --- | --- |\n")
        records.filterIsInstance<ExportRecord.TriggeredPattern>().forEach { r ->
            sb.append("| ").append(esc(r.patternId))
                .append(" | ").append(esc(r.name))
                .append(" | ").append(esc(r.scenarioCategory))
                .append(" | ").append(esc(r.version))
                .append(" | ").append(r.triggeredAt)
                .append(" | ").append(r.timesTriggered)
                .append(" |\n")
        }
    }

    private fun esc(s: String): String = s.replace("|", "\\|")

    private fun sortStable(records: List<ExportRecord>): List<ExportRecord> = records.sortedBy { stableKey(it) }

    private fun stableKey(record: ExportRecord): String =
        when (record) {
            is ExportRecord.SuspiciousNumber -> record.phoneLast4
            is ExportRecord.RiskCampaign -> record.campaignId
            is ExportRecord.TriggeredPattern -> record.patternId
        }
}
