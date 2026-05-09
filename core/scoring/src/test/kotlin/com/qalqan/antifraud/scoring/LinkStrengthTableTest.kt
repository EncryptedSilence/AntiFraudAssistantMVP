package com.qalqan.antifraud.scoring

import io.kotest.matchers.doubles.shouldBeExactly
import org.junit.jupiter.api.Test

class LinkStrengthTableTest {
    @Test fun `signal coefficients match spec section 10 dot 2`() {
        LinkStrengthTable.coefficient(LinkSignal.SAME_NUMBER) shouldBeExactly 1.0
        LinkStrengthTable.coefficient(LinkSignal.USER_CONFIRMED) shouldBeExactly 1.0
        LinkStrengthTable.coefficient(LinkSignal.SMS_AFTER_CALL) shouldBeExactly 0.9
        LinkStrengthTable.coefficient(LinkSignal.CALL_AFTER_SMS) shouldBeExactly 0.9
        LinkStrengthTable.coefficient(LinkSignal.SITE_AFTER_CALL_OR_SMS) shouldBeExactly 0.8
        LinkStrengthTable.coefficient(LinkSignal.MULTIPLE_UNKNOWN_24H) shouldBeExactly 0.7
        LinkStrengthTable.coefficient(LinkSignal.REPEATING_THEME) shouldBeExactly 0.7
        LinkStrengthTable.coefficient(LinkSignal.TEMPORAL_ONLY) shouldBeExactly 0.4
        LinkStrengthTable.coefficient(LinkSignal.WEAK) shouldBeExactly 0.2
    }

    @Test fun `combine takes the max coefficient across signals`() {
        LinkStrengthTable.combine(setOf(LinkSignal.WEAK, LinkSignal.SMS_AFTER_CALL)) shouldBeExactly 0.9
    }

    @Test fun `combine of empty set is 0`() {
        LinkStrengthTable.combine(emptySet()) shouldBeExactly 0.0
    }
}
