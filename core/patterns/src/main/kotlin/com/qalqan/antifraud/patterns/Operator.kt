package com.qalqan.antifraud.patterns

/**
 * Spec Appendix A — six operators allowed in pattern conditions.
 */
enum class Operator(val jsonValue: String) {
    EQUALS("equals"),
    IN("in"),
    GREATER_THAN("greaterThan"),
    LESS_THAN("lessThan"),
    CONTAINS("contains"),
    MATCHES("matches");

    companion object {
        fun fromJson(value: String): Operator? = entries.firstOrNull { it.jsonValue == value }
    }
}
