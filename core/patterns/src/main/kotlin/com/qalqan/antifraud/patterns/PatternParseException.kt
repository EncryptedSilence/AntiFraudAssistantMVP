package com.qalqan.antifraud.patterns

/**
 * Thrown by `PatternCatalogParser` when input JSON does not produce a valid `ScenarioPattern`.
 * The message is safe to surface to logs (no PII) but not to end users (it includes patternId
 * and field names — fine for diagnostics, not for §14 warning copy).
 */
class PatternParseException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
