package com.qalqan.antifraud.patterns

/**
 * Spec Appendix A `warning.level` enum. `low` is intentionally absent —
 * patterns only fire at medium or above per §11.5 banding.
 */
enum class WarningLevel(val jsonValue: String) {
    MEDIUM("medium"),
    HIGH("high"),
    CRITICAL("critical"),
    ;

    companion object {
        fun fromJson(value: String): WarningLevel? = entries.firstOrNull { it.jsonValue == value }
    }
}
