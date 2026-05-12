@file:Suppress("ReturnCount")

package com.qalqan.antifraud.web

import com.qalqan.antifraud.database.repository.WebEventRepository
import com.qalqan.antifraud.domain.DomainStatus
import java.time.Instant

/**
 * Spec §5.4 — single entry point for Stage 5's manual web-capture path.
 *
 * Pipeline:
 *   1. [DomainNormalizer.normalize] — strip URL components, lower-case, resolve eTLD+1
 *   2. [DomainSeenChecker.isNew]    — query the seen-list to decide `isNewDomain`
 *   3. [LookalikeDetector.match]    — return a [LookalikeMatch] within distance ≤ 2 if any
 *   4. [DomainStatusResolver.resolve] — map (isNew, lookalike) → [DomainStatus]
 *   5. [WebEventBuilder.build]      — compose the §16.4 [com.qalqan.antifraud.domain.WebEvent]
 *   6. persist via [WebEventRepository.save]
 *
 * Returns a [WebCaptureOutcome] for the UI to render. Action-log integration is wired
 * up in Phase 6 (T22); the post-site question hook lives in Phase 6 (T23).
 */
class WebManualCapture(
    private val normalizer: DomainNormalizer,
    private val detector: LookalikeDetector,
    private val seenChecker: DomainSeenChecker,
    private val builder: WebEventBuilder,
    private val repo: WebEventRepository,
    private val actionLog: WebObserverActionLog,
) {
    suspend fun submit(
        rawInput: String,
        visitedAt: Instant,
    ): WebCaptureOutcome {
        val normalized = normalizer.normalize(rawInput)
        when (normalized) {
            NormalizationResult.Error.Empty -> return WebCaptureOutcome.Rejected.Empty
            is NormalizationResult.Error.Invalid -> return WebCaptureOutcome.Rejected.Invalid(normalized.input)
            is NormalizationResult.Success -> { /* fall through */ }
        }
        val canonical = (normalized as NormalizationResult.Success).canonical
        val tentative =
            builder.build(
                canonical = canonical,
                visitedAt = visitedAt,
                isNew = true,
                status = DomainStatus.NEW,
            )
        val isNew = seenChecker.isNew(tentative.domainHash.value)
        val lookalike = detector.match(canonical)
        val status = DomainStatusResolver.resolve(isNew = isNew, lookalike = lookalike)
        val final = builder.build(canonical, visitedAt, isNew, status)
        repo.save(final)
        actionLog.manualSubmitted()
        lookalike?.let { actionLog.lookalikeTriggered(it.distance) }
        return WebCaptureOutcome.Saved(
            id = final.id,
            canonical = canonical,
            status = status,
            isNewDomain = isNew,
            lookalike = lookalike,
        )
    }
}
