package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.PatternId
import com.qalqan.antifraud.domain.ScenarioCategory

/**
 * Spec §6 + Appendix A — declarative scenario pattern.
 *
 * `triggeredCount`, `lastTriggeredAt`, `changeHistory`, `signatureStatus` from §6.2 are
 * deferred to Stage 6 (sync) / Stage 8 (UI); Stage 2 only ships the pure declarative shape.
 */
data class ScenarioPattern(
    val patternId: PatternId,
    val name: String,
    val description: String?,
    val category: ScenarioCategory,
    val version: String,
    val enabled: Boolean,
    val userCreated: Boolean,
    val source: String,
    val conditions: List<PatternCondition>,
    val correlation: Correlation,
    val warning: Warning,
    val recommendation: String?
) {
    init {
        require(name.isNotBlank()) { "name must not be blank" }
        require(name.length <= MAX_NAME_LENGTH) { "name must be at most $MAX_NAME_LENGTH chars" }
        description?.let {
            require(it.length <= MAX_DESCRIPTION_LENGTH) {
                "description must be at most $MAX_DESCRIPTION_LENGTH chars"
            }
        }
        require(SEMVER_REGEX.matches(version)) {
            "version must match semver pattern \\d+.\\d+.\\d+ (got '$version')"
        }
        require(source.isNotBlank()) { "source must not be blank" }
        require(conditions.isNotEmpty()) { "pattern must have at least one condition" }
        recommendation?.let {
            require(it.length <= MAX_RECOMMENDATION_LENGTH) {
                "recommendation must be at most $MAX_RECOMMENDATION_LENGTH chars"
            }
        }
    }

    companion object {
        const val MAX_NAME_LENGTH: Int = 120
        const val MAX_DESCRIPTION_LENGTH: Int = 1000
        const val MAX_RECOMMENDATION_LENGTH: Int = 600
        private val SEMVER_REGEX = Regex("""^\d+\.\d+\.\d+$""")
    }
}
