package com.qalqan.antifraud.web

/**
 * Spec §22 Stage 5 — curated list of KZ banks, government authorities, and telecoms a
 * fraudulent lookalike domain is most likely to imitate. Shipped in-APK as a Kotlin
 * constant; Stage 6 sync may later refresh from a signed update package.
 *
 * Audit guard: [LookalikeSeedCatalogTest] pins the size window (15..25) and a list of
 * required canonical entries so a typo or accidental deletion fails CI.
 *
 * Every entry must be a lowercase eTLD+1 string that survives [DomainNormalizer.normalize]
 * unchanged. The matcher in [LookalikeDetector] compares against these directly.
 */
object LookalikeSeedCatalog {
    val seeds: Set<String> =
        setOf(
            // KZ banks
            "halykbank.kz",
            "kaspi.kz",
            "jusan.kz",
            "forte.kz",
            "bcc.kz",
            "bereke.kz",
            "freedombank.kz",
            "eubank.kz",
            "sberbank.kz",
            "alfabank.kz",
            "vtb.kz",
            "altyn-i.kz",
            // KZ government / authority
            "egov.kz",
            "kgd.gov.kz",
            // KZ telecoms (frequently impersonated for SIM-swap / OTP attacks)
            "beeline.kz",
            "kcell.kz",
            "tele2.kz",
            "activ.kz",
        )
}
