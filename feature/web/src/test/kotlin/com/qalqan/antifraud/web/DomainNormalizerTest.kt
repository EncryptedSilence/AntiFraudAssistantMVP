package com.qalqan.antifraud.web

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test

class DomainNormalizerTest {
    private val n = DomainNormalizer()

    @Test
    fun `bare eTLD plus 1 is returned unchanged`() {
        n.normalize("halykbank.kz") shouldBe NormalizationResult.Success("halykbank.kz")
    }

    @Test
    fun `www subdomain is stripped`() {
        n.normalize("www.halykbank.kz") shouldBe NormalizationResult.Success("halykbank.kz")
    }

    @Test
    fun `scheme + path + query + fragment + userinfo + port are all stripped`() {
        val r = n.normalize("https://user:pass@WWW.HalykBank.kz:443/login?next=x#top")
        r shouldBe NormalizationResult.Success("halykbank.kz")
    }

    @Test
    fun `deep subdomain collapses to eTLD plus 1`() {
        n.normalize("a.b.c.kaspi.kz") shouldBe NormalizationResult.Success("kaspi.kz")
    }

    @Test
    fun `multi-label public suffix is honored`() {
        // gov.kz is a public suffix, so eTLD+1 is "xyz.gov.kz", not "gov.kz".
        n.normalize("portal.xyz.gov.kz") shouldBe NormalizationResult.Success("xyz.gov.kz")
    }

    @Test
    fun `unknown TLD falls back to rightmost two labels`() {
        // .example is not in the snapshot, so default-of-two applies.
        n.normalize("foo.bar.example") shouldBe NormalizationResult.Success("bar.example")
    }

    @Test
    fun `bare TLD without a registrable label is invalid`() {
        val r = n.normalize("kz")
        r.shouldBeInstanceOf<NormalizationResult.Error.Invalid>()
    }
}
