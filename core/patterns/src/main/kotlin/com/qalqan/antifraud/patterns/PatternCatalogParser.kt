package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.PatternId
import com.qalqan.antifraud.domain.ScenarioCategory
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException

/**
 * Spec §6 / Appendix A — parses pattern JSON into typed `ScenarioPattern`.
 *
 * Stage 2 uses Moshi reflective adapters + the `init {}` invariants from Phase 2
 * in lieu of a JSON Schema validator runtime. Unknown operators, unsupported event
 * types, and out-of-range values all surface as `PatternParseException`.
 */
object PatternCatalogParser {
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val singleAdapter: JsonAdapter<RawPattern> = moshi.adapter(RawPattern::class.java)
    private val listAdapter: JsonAdapter<List<RawPattern>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, RawPattern::class.java))

    @Suppress("ThrowsCount")
    fun fromJson(json: String): ScenarioPattern {
        val raw =
            try {
                singleAdapter.fromJson(json)
                    ?: throw PatternParseException("pattern JSON deserialized to null")
            } catch (e: JsonDataException) {
                throw PatternParseException("malformed pattern JSON: ${e.message}", e)
            } catch (e: IOException) {
                throw PatternParseException("malformed pattern JSON: ${e.message ?: e.toString()}", e)
            }
        return raw.toDomain()
    }

    @Suppress("ThrowsCount")
    fun listFromJson(json: String): List<ScenarioPattern> {
        val raw =
            try {
                listAdapter.fromJson(json)
                    ?: throw PatternParseException("pattern list JSON deserialized to null")
            } catch (e: JsonDataException) {
                throw PatternParseException("malformed pattern list JSON: ${e.message}", e)
            } catch (e: IOException) {
                throw PatternParseException("malformed pattern list JSON: ${e.message ?: e.toString()}", e)
            }
        return raw.map { it.toDomain() }
    }
}

private data class RawPattern(
    val patternId: String?,
    val name: String?,
    val description: String?,
    val category: String?,
    val version: String?,
    val enabled: Boolean?,
    val userCreated: Boolean? = false,
    val source: String? = "system",
    val conditions: List<RawCondition>?,
    val correlation: RawCorrelation? = null,
    val warning: RawWarning?,
    val recommendation: String? = null,
) {
    @Suppress("ThrowsCount")
    fun toDomain(): ScenarioPattern {
        val pid = patternId ?: throw PatternParseException("missing patternId")
        val resolvedCategory =
            ScenarioCategoryMapping.fromJson(category)
                ?: throw PatternParseException("unknown or missing category for pattern '$pid'")
        val resolvedWarning =
            warning?.toDomain(pid)
                ?: throw PatternParseException("missing warning for pattern '$pid'")
        val resolvedConditions =
            conditions?.mapIndexed { i, c -> c.toDomain(pid, i) }
                ?: throw PatternParseException("missing conditions for pattern '$pid'")
        return try {
            ScenarioPattern(
                patternId = PatternId(pid),
                name = name ?: throw PatternParseException("missing name for pattern '$pid'"),
                description = description,
                category = resolvedCategory,
                version = version ?: throw PatternParseException("missing version for pattern '$pid'"),
                enabled = enabled ?: throw PatternParseException("missing enabled for pattern '$pid'"),
                userCreated = userCreated ?: false,
                source = source ?: "system",
                conditions = resolvedConditions,
                correlation = correlation?.toDomain() ?: Correlation(),
                warning = resolvedWarning,
                recommendation = recommendation,
            )
        } catch (e: IllegalArgumentException) {
            throw PatternParseException("invalid pattern '$pid': ${e.message}", e)
        }
    }
}

private data class RawCondition(
    val eventType: String?,
    val field: String?,
    val operator: String?,
    // `value` arrives as Boolean/Number/String/List depending on the operator; type validation
    // is deferred to ConditionEvaluator/EventFieldAccessor in Phase 4.
    val value: Any?,
    val weight: Int?,
    val timeWindowHours: Int? = null,
) {
    @Suppress("ThrowsCount")
    fun toDomain(
        patternId: String,
        index: Int,
    ): PatternCondition {
        val et = eventType ?: throw PatternParseException("missing eventType in pattern '$patternId' condition $index")
        val resolvedEventType =
            EventType.fromJson(et)
                ?: throw PatternParseException("unknown eventType '$et' in pattern '$patternId' condition $index")
        val op = operator ?: throw PatternParseException("missing operator in pattern '$patternId' condition $index")
        val resolvedOperator =
            Operator.fromJson(op)
                ?: throw PatternParseException("unknown operator '$op' in pattern '$patternId' condition $index")
        val v = value ?: throw PatternParseException("missing value in pattern '$patternId' condition $index")
        val w = weight ?: throw PatternParseException("missing weight in pattern '$patternId' condition $index")
        return try {
            PatternCondition(
                eventType = resolvedEventType,
                field = field ?: throw PatternParseException("missing field in pattern '$patternId' condition $index"),
                operator = resolvedOperator,
                value = v,
                weight = w,
                timeWindowHours = timeWindowHours,
            )
        } catch (e: IllegalArgumentException) {
            throw PatternParseException("invalid condition $index in pattern '$patternId': ${e.message}", e)
        }
    }
}

private data class RawCorrelation(
    val maxCampaignAgeDays: Int? = null,
    val linkStrength: Double? = null,
) {
    fun toDomain(): Correlation =
        Correlation(
            maxCampaignAgeDays = maxCampaignAgeDays ?: Correlation.DEFAULT_MAX_AGE_DAYS,
            linkStrength = linkStrength ?: 0.0,
        )
}

private data class RawWarning(
    val level: String?,
    val title: String?,
    val message: String?,
) {
    @Suppress("ThrowsCount")
    fun toDomain(patternId: String): Warning {
        val l = level ?: throw PatternParseException("missing warning.level for pattern '$patternId'")
        val resolvedLevel =
            WarningLevel.fromJson(l)
                ?: throw PatternParseException("unknown warning.level '$l' for pattern '$patternId'")
        return try {
            Warning(
                level = resolvedLevel,
                title = title ?: throw PatternParseException("missing warning.title for pattern '$patternId'"),
                message = message ?: throw PatternParseException("missing warning.message for pattern '$patternId'"),
            )
        } catch (e: IllegalArgumentException) {
            throw PatternParseException("invalid warning for pattern '$patternId': ${e.message}", e)
        }
    }
}

private object ScenarioCategoryMapping {
    private val byJson: Map<String, ScenarioCategory> =
        mapOf(
            "bankFraud" to ScenarioCategory.BANK_FRAUD,
            "authoritySpoof" to ScenarioCategory.AUTHORITY_SPOOF,
            "investmentScheme" to ScenarioCategory.INVESTMENT_SCHEME,
            "deliveryScam" to ScenarioCategory.DELIVERY_SCAM,
            "techSupportScam" to ScenarioCategory.TECH_SUPPORT_SCAM,
            "unknownSocialEngineering" to ScenarioCategory.UNKNOWN_SOCIAL_ENGINEERING,
        )

    fun fromJson(value: String?): ScenarioCategory? = value?.let { byJson[it] }
}
