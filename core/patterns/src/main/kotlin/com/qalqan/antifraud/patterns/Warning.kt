package com.qalqan.antifraud.patterns

/**
 * Spec Appendix A `warning` — title ≤80, message ≤600.
 */
data class Warning(
    val level: WarningLevel,
    val title: String,
    val message: String,
) {
    init {
        require(title.isNotBlank()) { "title must not be blank" }
        require(message.isNotBlank()) { "message must not be blank" }
        require(title.length <= MAX_TITLE_LENGTH) {
            "title must be at most $MAX_TITLE_LENGTH chars"
        }
        require(message.length <= MAX_MESSAGE_LENGTH) {
            "message must be at most $MAX_MESSAGE_LENGTH chars"
        }
    }

    companion object {
        const val MAX_TITLE_LENGTH: Int = 80
        const val MAX_MESSAGE_LENGTH: Int = 600
    }
}
