package com.qalqan.antifraud.web

/**
 * Spec §5.4 / §16.4 — outcome of normalizing user-typed domain input.
 *
 * [Success.canonical] holds an eTLD+1 lowercase string with no scheme, path, query,
 * fragment, user-info, port, or whitespace. The §16.4 `WebEvent.init` invariant rejects
 * anything else, so the orchestrator can pass `canonical` straight through.
 */
sealed interface NormalizationResult {
    data class Success(val canonical: String) : NormalizationResult

    sealed interface Error : NormalizationResult {
        data object Empty : Error
        data class Invalid(val input: String) : Error
    }
}
