package com.qalqan.antifraud.patterns

/**
 * Spec Appendix A `correlation` — `maxCampaignAgeDays` ∈ [1, 14], `linkStrength` ∈ [0, 1].
 * Defaults align with the §11.4 14-day active horizon.
 */
data class Correlation(
    val maxCampaignAgeDays: Int = DEFAULT_MAX_AGE_DAYS,
    val linkStrength: Double = 0.0,
) {
    init {
        require(maxCampaignAgeDays in MIN_MAX_AGE_DAYS..DEFAULT_MAX_AGE_DAYS) {
            "maxCampaignAgeDays must be in $MIN_MAX_AGE_DAYS..$DEFAULT_MAX_AGE_DAYS"
        }
        require(linkStrength in 0.0..1.0) { "linkStrength must be in 0..1" }
    }

    companion object {
        const val MIN_MAX_AGE_DAYS: Int = 1
        const val DEFAULT_MAX_AGE_DAYS: Int = 14
    }
}
