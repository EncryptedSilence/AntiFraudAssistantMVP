package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PatternId
import com.qalqan.antifraud.domain.ScenarioCategory
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PatternExplainerTest {
    private val warningHigh = Warning(WarningLevel.HIGH, "Possible fraud", "Do not share codes")
    private val warningCritical = Warning(WarningLevel.CRITICAL, "High risk", "Do not transfer money")

    private fun pattern(
        id: String,
        warning: Warning = warningHigh,
        conditions: List<PatternCondition> =
            listOf(
                PatternCondition(EventType.CALL_EVENT, "isKnownContact", Operator.EQUALS, false, weight = 20),
            ),
    ) = ScenarioPattern(
        patternId = PatternId(id),
        name = "test", description = null, category = ScenarioCategory.BANK_FRAUD,
        version = "1.0.0", enabled = true, userCreated = false, source = "system",
        conditions = conditions, correlation = Correlation(),
        warning = warning, recommendation = null,
    )

    @Test
    fun `explanation level is the max across triggered patterns`() {
        val matches =
            listOf(
                pattern("p1", warning = warningHigh) to
                    MatchResult(PatternId("p1"), true, 30, listOf(EventId("e1"))),
                pattern("p2", warning = warningCritical) to
                    MatchResult(PatternId("p2"), true, 40, listOf(EventId("e2"))),
            )
        PatternExplainer.explain(matches).level shouldBe WarningLevel.CRITICAL
    }

    @Test
    fun `explainer emits at least 3 reasons when at least 3 conditions triggered`() {
        val patternWith3Conds =
            pattern(
                "p1",
                conditions =
                    listOf(
                        PatternCondition(EventType.CALL_EVENT, "isKnownContact", Operator.EQUALS, false, weight = 20),
                        PatternCondition(EventType.SMS_EVENT, "containsCode", Operator.EQUALS, true, weight = 30),
                        PatternCondition(EventType.SMS_EVENT, "smsCategory", Operator.EQUALS, "OTP", weight = 25),
                    ),
            )
        val matches =
            listOf(
                patternWith3Conds to
                    MatchResult(
                        PatternId("p1"), true, 60,
                        listOf(EventId("e1"), EventId("e2"), EventId("e3")),
                    ),
            )
        val ex = PatternExplainer.explain(matches)
        ex.reasons shouldHaveAtLeastSize 3
    }

    @Test
    fun `explainer emits all available when fewer than 3 conditions triggered`() {
        val matches =
            listOf(
                pattern("p1") to MatchResult(PatternId("p1"), true, 20, listOf(EventId("e1"))),
            )
        val ex = PatternExplainer.explain(matches)
        ex.reasons shouldHaveSize 1
    }

    @Test
    fun `explanations are deduplicated by text`() {
        val pat =
            pattern(
                "p1",
                conditions =
                    listOf(
                        PatternCondition(EventType.CALL_EVENT, "isKnownContact", Operator.EQUALS, false, weight = 20),
                        PatternCondition(EventType.CALL_EVENT, "isKnownContact", Operator.EQUALS, false, weight = 10),
                    ),
            )
        val matches = listOf(pat to MatchResult(PatternId("p1"), true, 30, listOf(EventId("e1"), EventId("e1"))))
        val ex = PatternExplainer.explain(matches)
        ex.reasons.map { it.text }.toSet().size shouldBe ex.reasons.size
    }
}
