package com.qalqan.antifraud.web

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.Test

class PublicSuffixSnapshotTest {
    @Test
    fun `snapshot contains the KZ public suffixes Stage 5 cares about`() {
        PublicSuffixSnapshot.suffixes shouldContain "kz"
        PublicSuffixSnapshot.suffixes shouldContain "gov.kz"
        PublicSuffixSnapshot.suffixes shouldContain "edu.kz"
        PublicSuffixSnapshot.suffixes shouldContain "com.kz"
    }

    @Test
    fun `snapshot contains the common gTLDs Stage 5 cares about`() {
        listOf("com", "org", "net", "io").forEach { suffix ->
            PublicSuffixSnapshot.suffixes shouldContain suffix
        }
    }

    @Test
    fun `snapshot is sorted longest-first so the matcher finds the most-specific match`() {
        val lengths = PublicSuffixSnapshot.suffixes.map { it.count { ch -> ch == '.' } }
        // Longest (most dots) first; allow ties.
        lengths shouldBe lengths.sortedDescending()
    }
}
