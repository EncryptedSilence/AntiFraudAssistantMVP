package com.qalqan.antifraud.settings

import com.qalqan.antifraud.domain.QuestionCode

/**
 * Spec §5.5.2 — the three irreducible questions surfaced in the UI. Maps 1:1 to
 * [com.qalqan.antifraud.domain.QuestionCode] (the persistence-layer enum used by
 * `UserAnswerRepository`).
 */
enum class QuestionPromptKind(val code: QuestionCode) {
    /** §5.5.2 Q1 — Did the caller claim to be from a bank, government, or other official service? */
    CALLER_IDENTITY(QuestionCode.Q1_CALLER_OFFICIAL_CLAIM),

    /** §5.5.2 Q2 — Did they tell you not to call back / consult anyone / pressure you to hurry? */
    PRESSURE(QuestionCode.Q2_PRESSURE_OR_DONT_CONSULT),

    /** §5.5.2 Q3 — Did they ask you to share a code / install an app / open a link / transfer money? */
    ACTION_REQUEST(QuestionCode.Q3_ASKED_TO_ACT_NOW),
    ;

    companion object {
        fun fromCode(code: QuestionCode): QuestionPromptKind = entries.first { it.code == code }
    }
}
