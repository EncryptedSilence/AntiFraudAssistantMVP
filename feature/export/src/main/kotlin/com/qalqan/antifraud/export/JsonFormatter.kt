package com.qalqan.antifraud.export

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.Instant

/**
 * Spec §8.5 — JSON formatter. One root object with one key per requested category
 * (using `ExportCategory.jsonValue` as the key); each value is an array of records
 * for that category. Empty categories are emitted as `[]` to keep the schema stable.
 *
 * Determinism: stable-sort by per-variant key, Moshi reflective adapter with explicit
 * `serializeNulls()` so null fields round-trip identically across runs. UTF-8, no BOM,
 * no pretty-printing — single line per call. The output is byte-identical for repeated
 * calls with the same input.
 */
internal object JsonFormatter : ExportFormatter {
    override val format = ExportFormat.JSON

    private val moshi: Moshi =
        Moshi.Builder()
            .add(InstantAdapter)
            .addLast(KotlinJsonAdapterFactory())
            .build()

    private val rootAdapter =
        moshi
            .adapter<Map<String, List<JsonRecord>>>(
                Types.newParameterizedType(
                    Map::class.java,
                    String::class.java,
                    Types.newParameterizedType(List::class.java, JsonRecord::class.java),
                ),
            )
            .serializeNulls()

    override fun format(
        records: List<ExportRecord>,
        request: ExportRequest,
    ): ByteArray {
        val byCategory = records.groupBy { it.category }
        val root = linkedMapOf<String, List<JsonRecord>>()
        request.categories.forEach { category ->
            val sorted = sortStable(byCategory[category].orEmpty()).map { toJson(it) }
            root[category.jsonValue] = sorted
        }
        return rootAdapter.toJson(root).toByteArray(Charsets.UTF_8)
    }

    private fun sortStable(records: List<ExportRecord>): List<ExportRecord> = records.sortedBy { stableKey(it) }

    private fun stableKey(record: ExportRecord): String =
        when (record) {
            is ExportRecord.SuspiciousNumber -> record.phoneLast4
            is ExportRecord.RiskCampaign -> record.campaignId
            is ExportRecord.TriggeredPattern -> record.patternId
        }

    private fun toJson(record: ExportRecord): JsonRecord =
        when (record) {
            is ExportRecord.SuspiciousNumber ->
                JsonRecord(
                    phoneFull = record.phoneFull,
                    phoneLast4 = record.phoneLast4,
                    isShortCode = record.isShortCode,
                    displayName = record.displayName,
                    trustStatus = record.trustStatus,
                    firstSeenAt = record.firstSeenAt,
                    riskCounter = record.riskCounter,
                )
            is ExportRecord.RiskCampaign ->
                JsonRecord(
                    campaignId = record.campaignId,
                    startedAt = record.startedAt,
                    lastEventAt = record.lastEventAt,
                    status = record.status,
                    scenarioType = record.scenarioType,
                    campaignRiskScore = record.campaignRiskScore,
                    campaignRiskLevel = record.campaignRiskLevel,
                    relatedEventCount = record.relatedEventCount,
                    explanation = record.explanation,
                )
            is ExportRecord.TriggeredPattern ->
                JsonRecord(
                    patternId = record.patternId,
                    name = record.name,
                    scenarioCategory = record.scenarioCategory,
                    version = record.version,
                    triggeredAt = record.triggeredAt,
                    timesTriggered = record.timesTriggered,
                )
        }

    @Suppress("LongParameterList")
    @JsonClass(generateAdapter = false)
    internal data class JsonRecord(
        val phoneFull: String? = null,
        val phoneLast4: String? = null,
        val isShortCode: Boolean? = null,
        val displayName: String? = null,
        val trustStatus: String? = null,
        val firstSeenAt: Instant? = null,
        val riskCounter: Int? = null,
        val campaignId: String? = null,
        val startedAt: Instant? = null,
        val lastEventAt: Instant? = null,
        val status: String? = null,
        val scenarioType: String? = null,
        val campaignRiskScore: Int? = null,
        val campaignRiskLevel: String? = null,
        val relatedEventCount: Int? = null,
        val explanation: String? = null,
        val patternId: String? = null,
        val name: String? = null,
        val scenarioCategory: String? = null,
        val version: String? = null,
        val triggeredAt: Instant? = null,
        val timesTriggered: Int? = null,
    )

    internal object InstantAdapter {
        @com.squareup.moshi.FromJson fun fromJson(value: String): Instant = Instant.parse(value)

        @com.squareup.moshi.ToJson fun toJson(value: Instant): String = value.toString()
    }
}
