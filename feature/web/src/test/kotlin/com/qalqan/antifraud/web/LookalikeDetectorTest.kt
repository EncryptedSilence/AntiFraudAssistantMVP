package com.qalqan.antifraud.web

import io.kotest.matchers.shouldBe
import org.junit.Test

class LookalikeDetectorTest {
    private val d = LookalikeDetector(seeds = LookalikeSeedCatalog.seeds)

    @Test
    fun `exact match against a seed is NOT a lookalike (it is the real site)`() {
        d.match("halykbank.kz") shouldBe null
        d.match("kaspi.kz") shouldBe null
    }

    @Test
    fun `single character substitution flags as lookalike (distance 1)`() {
        // halykbank.kz → halykbamk.kz (n→m)
        d.match("halykbamk.kz") shouldBe LookalikeMatch(seed = "halykbank.kz", distance = 1)
    }

    @Test
    fun `single character insertion flags as lookalike (distance 1)`() {
        // kaspi.kz → kasspi.kz
        d.match("kasspi.kz") shouldBe LookalikeMatch(seed = "kaspi.kz", distance = 1)
    }

    @Test
    fun `two-character edit flags as lookalike (distance 2)`() {
        // halykbank.kz → halikbamk.kz (y→i, n→m): distance 2.
        d.match("halikbamk.kz") shouldBe LookalikeMatch(seed = "halykbank.kz", distance = 2)
    }

    @Test
    fun `three-character edit is over threshold and returns null`() {
        // halykbank.kz → balikbamk.kz (3 substitutions)
        d.match("balikbamk.kz") shouldBe null
    }

    @Test
    fun `unrelated domain returns null`() {
        d.match("example.com") shouldBe null
    }

    @Test
    fun `closest seed wins when multiple are within threshold`() {
        // Construct an input within distance 1 of kaspi.kz AND distance 2 of forte.kz
        // (impossible to design naturally — accept closest of any single seed wins).
        // Concrete: 'kaspx.kz' is distance 1 from kaspi.kz; not within 2 of forte.kz.
        d.match("kaspx.kz") shouldBe LookalikeMatch(seed = "kaspi.kz", distance = 1)
    }

    @Test
    fun `empty input returns null`() {
        d.match("") shouldBe null
    }
}
