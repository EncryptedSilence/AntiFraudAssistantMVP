package com.qalqan.antifraud.scoring

import com.qalqan.antifraud.domain.AnswerCode
import com.qalqan.antifraud.domain.QuestionCode
import com.qalqan.antifraud.domain.UserAnswer
import kotlin.math.min

/**
 * Spec §12.4: each question carries a max delta. We take the MAX across the answer set (not the sum)
 * so the irreducible-question reframe in §5.5 cannot single-handedly blow up EventRisk.
 * The max delta cap is 50.
 */
object UserAnswerRisk {
    private const val CAP = 50

    fun compute(answers: List<UserAnswer>): Int {
        if (answers.isEmpty()) return 0
        val perAnswer = answers.map(::deltaFor)
        val best = perAnswer.maxOrNull() ?: 0
        return min(best, CAP)
    }

    private fun deltaFor(a: UserAnswer): Int {
        val base = when (a.questionCode) {
            QuestionCode.Q1_CALLER_OFFICIAL_CLAIM -> 25
            QuestionCode.Q2_PRESSURE_OR_DONT_CONSULT -> 40
            QuestionCode.Q3_ASKED_TO_ACT_NOW -> 50
        }
        return when (a.answerCode) {
            AnswerCode.YES -> base
            AnswerCode.NOT_SURE -> base / 2
            AnswerCode.NO, AnswerCode.NOT_ANSWERED -> 0
        }
    }
}
