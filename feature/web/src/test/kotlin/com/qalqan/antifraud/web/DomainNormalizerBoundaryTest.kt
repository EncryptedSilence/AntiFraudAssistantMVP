package com.qalqan.antifraud.web

import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Spec §16.4 / §23 #24 / §2.1 — the privacy hard rule.
 *
 * The normalized canonical produced by [DomainNormalizer] is what gets persisted in
 * [com.qalqan.antifraud.domain.WebEvent.domainDisplayLocal]. The `WebEvent.init` block
 * already rejects '/', '?', '#', and "://", but the boundary test runs at the input
 * boundary so a regression surfaces here first.
 */
class DomainNormalizerBoundaryTest {
    private val n = DomainNormalizer()

    @Test
    fun `output never contains a path slash`() {
        forbiddenInputs.forEach { input ->
            val r = n.normalize(input)
            if (r is NormalizationResult.Success) {
                ('/' in r.canonical) shouldBe false
            }
        }
    }

    @Test
    fun `output never contains a query mark`() {
        forbiddenInputs.forEach { input ->
            val r = n.normalize(input)
            if (r is NormalizationResult.Success) {
                ('?' in r.canonical) shouldBe false
            }
        }
    }

    @Test
    fun `output never contains a fragment hash`() {
        forbiddenInputs.forEach { input ->
            val r = n.normalize(input)
            if (r is NormalizationResult.Success) {
                ('#' in r.canonical) shouldBe false
            }
        }
    }

    @Test
    fun `output never contains a scheme delimiter`() {
        forbiddenInputs.forEach { input ->
            val r = n.normalize(input)
            if (r is NormalizationResult.Success) {
                r.canonical.contains("://") shouldBe false
            }
        }
    }

    @Test
    fun `output never contains userinfo`() {
        forbiddenInputs.forEach { input ->
            val r = n.normalize(input)
            if (r is NormalizationResult.Success) {
                ('@' in r.canonical) shouldBe false
            }
        }
    }

    @Test
    fun `output never contains a port`() {
        forbiddenInputs.forEach { input ->
            val r = n.normalize(input)
            if (r is NormalizationResult.Success) {
                (':' in r.canonical) shouldBe false
            }
        }
    }

    @Test
    fun `output never contains whitespace`() {
        forbiddenInputs.forEach { input ->
            val r = n.normalize(input)
            if (r is NormalizationResult.Success) {
                r.canonical.any(Char::isWhitespace) shouldBe false
            }
        }
    }

    private companion object {
        val forbiddenInputs: List<String> =
            listOf(
                "https://halykbank.kz/login?next=https://evil.kz#x",
                "http://halykbank.kz/path/with/slashes",
                "https://www.halykbank.kz/?token=abcd",
                "halykbank.kz/path",
                "halykbank.kz?q=1",
                "halykbank.kz#anchor",
                "user:secret@halykbank.kz:8443/admin",
                "halykbank.kz:443",
                "HALYK BANK.kz",
                "https://kaspi.kz/  ",
            )
    }
}
