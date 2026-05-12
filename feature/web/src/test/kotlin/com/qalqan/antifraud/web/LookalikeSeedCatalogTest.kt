package com.qalqan.antifraud.web

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import org.junit.Test

class LookalikeSeedCatalogTest {
    @Test
    fun `catalog ships within the expected size window`() {
        // §22 Stage 5 plan: "~20 KZ banks/services". Allow ±5 so a future audit can
        // tweak without breaking the contract.
        LookalikeSeedCatalog.seeds.size shouldBeGreaterThanOrEqual MIN_SEEDS
        LookalikeSeedCatalog.seeds.size shouldBeLessThanOrEqual MAX_SEEDS
    }

    @Test
    fun `catalog contains the canonical KZ banks Stage 5 must recognize`() {
        LookalikeSeedCatalog.seeds shouldContainAll
            setOf(
                "halykbank.kz",
                "kaspi.kz",
                "jusan.kz",
                "forte.kz",
                "bcc.kz",
                "bereke.kz",
                "freedombank.kz",
                "eubank.kz",
                "sberbank.kz",
            )
    }

    @Test
    fun `catalog contains KZ authority + telecom anchors`() {
        LookalikeSeedCatalog.seeds shouldContainAll
            setOf(
                "egov.kz",
                "kgd.gov.kz",
                "beeline.kz",
                "kcell.kz",
                "tele2.kz",
                "activ.kz",
            )
    }

    @Test
    fun `every seed is already eTLD plus 1 lowercase`() {
        LookalikeSeedCatalog.seeds.forEach { seed ->
            ('/' in seed) shouldBe false
            ('?' in seed) shouldBe false
            (seed == seed.lowercase()) shouldBe true
            (seed.count { it == '.' } in 1..3) shouldBe true
        }
    }

    private companion object {
        const val MIN_SEEDS = 15
        const val MAX_SEEDS = 25
    }
}
