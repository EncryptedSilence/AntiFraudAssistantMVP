package com.qalqan.antifraud.scoring

import io.kotest.matchers.doubles.shouldBeExactly
import org.junit.jupiter.api.Test
import java.time.Duration

class TimeDecayTableTest {
    @Test fun `0 to 24 hours is 1`() {
        TimeDecayTable.coefficient(Duration.ofHours(0)) shouldBeExactly 1.0
        TimeDecayTable.coefficient(Duration.ofHours(23).plusMinutes(59)) shouldBeExactly 1.0
        TimeDecayTable.coefficient(Duration.ofHours(24)) shouldBeExactly 1.0
    }

    @Test fun `2 to 3 days is 0 dot 8`() {
        TimeDecayTable.coefficient(Duration.ofHours(48)) shouldBeExactly 0.8
        TimeDecayTable.coefficient(Duration.ofDays(3)) shouldBeExactly 0.8
    }

    @Test fun `4 to 7 days is 0 dot 6`() {
        TimeDecayTable.coefficient(Duration.ofDays(4)) shouldBeExactly 0.6
        TimeDecayTable.coefficient(Duration.ofDays(7)) shouldBeExactly 0.6
    }

    @Test fun `8 to 14 days is 0 dot 4`() {
        TimeDecayTable.coefficient(Duration.ofDays(8)) shouldBeExactly 0.4
        TimeDecayTable.coefficient(Duration.ofDays(14)) shouldBeExactly 0.4
    }

    @Test fun `over 14 days is 0`() {
        TimeDecayTable.coefficient(Duration.ofDays(15)) shouldBeExactly 0.0
        TimeDecayTable.coefficient(Duration.ofDays(365)) shouldBeExactly 0.0
    }

    @Test fun `negative duration treated as zero`() {
        TimeDecayTable.coefficient(Duration.ofHours(-1)) shouldBeExactly 1.0
    }
}
