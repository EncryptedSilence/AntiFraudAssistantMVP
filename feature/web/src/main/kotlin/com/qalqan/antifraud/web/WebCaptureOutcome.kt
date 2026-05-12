package com.qalqan.antifraud.web

import com.qalqan.antifraud.domain.DomainStatus
import com.qalqan.antifraud.domain.EventId

/**
 * Spec §5.4 — outcome of [WebManualCapture.submit]. `Saved` is the success case and
 * carries the signals the orchestrator decided (status, isNewDomain, lookalike) so the
 * caller can render the confirmation toast without re-running the pipeline. `Rejected`
 * variants are surfaced to the UI as a quiet error (no toast for empty; one-line error
 * for invalid).
 */
sealed interface WebCaptureOutcome {
    data class Saved(
        val id: EventId,
        val canonical: String,
        val status: DomainStatus,
        val isNewDomain: Boolean,
        val lookalike: LookalikeMatch?,
    ) : WebCaptureOutcome

    sealed interface Rejected : WebCaptureOutcome {
        data object Empty : Rejected

        data class Invalid(val input: String) : Rejected
    }
}
