package com.qalqan.antifraud.export

/**
 * Spec §8.5 — CSV formatter, RFC 4180 compliant. Lines end with CRLF; fields containing
 * a comma, double-quote, or CR / LF are wrapped in double quotes and embedded quotes are
 * doubled. Each category emits a `# <jsonValue>` header line so multi-category exports
 * stay parseable by a downstream consumer.
 *
 * Deterministic: stable-sort within each category, fixed column order per variant.
 */
internal object CsvFormatter : ExportFormatter {
    override val format = ExportFormat.CSV
    private const val CRLF = "\r\n"

    override fun format(
        records: List<ExportRecord>,
        request: ExportRequest,
    ): ByteArray {
        val sb = StringBuilder()
        val byCategory = records.groupBy { it.category }
        request.categories.forEach { category ->
            sb.append("# ").append(category.jsonValue).append(CRLF)
            val sorted = sortStable(byCategory[category].orEmpty())
            when (category) {
                ExportCategory.SUSPICIOUS_NUMBERS -> writeSuspiciousNumbers(sb, sorted)
                ExportCategory.RISK_CAMPAIGNS -> writeRiskCampaigns(sb, sorted)
                ExportCategory.TRIGGERED_PATTERNS -> writeTriggeredPatterns(sb, sorted)
            }
        }
        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    private fun writeSuspiciousNumbers(
        sb: StringBuilder,
        records: List<ExportRecord>,
    ) {
        sb.append("phoneFull,phoneLast4,isShortCode,displayName,trustStatus,firstSeenAt,riskCounter").append(CRLF)
        records.filterIsInstance<ExportRecord.SuspiciousNumber>().forEach { r ->
            sb.append(field(r.phoneFull.orEmpty())).append(',')
                .append(field(r.phoneLast4)).append(',')
                .append(r.isShortCode).append(',')
                .append(field(r.displayName.orEmpty())).append(',')
                .append(field(r.trustStatus)).append(',')
                .append(r.firstSeenAt).append(',')
                .append(r.riskCounter).append(CRLF)
        }
    }

    @Suppress("MaxLineLength")
    private fun writeRiskCampaigns(
        sb: StringBuilder,
        records: List<ExportRecord>,
    ) {
        sb.append("campaignId,startedAt,lastEventAt,status,scenarioType,")
            .append("campaignRiskScore,campaignRiskLevel,relatedEventCount,explanation").append(CRLF)
        records.filterIsInstance<ExportRecord.RiskCampaign>().forEach { r ->
            sb.append(field(r.campaignId)).append(',')
                .append(r.startedAt).append(',')
                .append(r.lastEventAt).append(',')
                .append(field(r.status)).append(',')
                .append(field(r.scenarioType)).append(',')
                .append(r.campaignRiskScore).append(',')
                .append(field(r.campaignRiskLevel)).append(',')
                .append(r.relatedEventCount).append(',')
                .append(field(r.explanation)).append(CRLF)
        }
    }

    private fun writeTriggeredPatterns(
        sb: StringBuilder,
        records: List<ExportRecord>,
    ) {
        sb.append("patternId,name,category,version,triggeredAt,timesTriggered").append(CRLF)
        records.filterIsInstance<ExportRecord.TriggeredPattern>().forEach { r ->
            sb.append(field(r.patternId)).append(',')
                .append(field(r.name)).append(',')
                .append(field(r.scenarioCategory)).append(',')
                .append(field(r.version)).append(',')
                .append(r.triggeredAt).append(',')
                .append(r.timesTriggered).append(CRLF)
        }
    }

    /** RFC 4180 §2.6 — quote if the field contains a comma, double-quote, CR or LF. */
    private fun field(value: String): String {
        val needsQuoting = value.any { it == ',' || it == '"' || it == '\r' || it == '\n' }
        return if (!needsQuoting) value else "\"${value.replace("\"", "\"\"")}\""
    }

    private fun sortStable(records: List<ExportRecord>): List<ExportRecord> = records.sortedBy { stableKey(it) }

    private fun stableKey(record: ExportRecord): String =
        when (record) {
            is ExportRecord.SuspiciousNumber -> record.phoneLast4
            is ExportRecord.RiskCampaign -> record.campaignId
            is ExportRecord.TriggeredPattern -> record.patternId
        }
}
