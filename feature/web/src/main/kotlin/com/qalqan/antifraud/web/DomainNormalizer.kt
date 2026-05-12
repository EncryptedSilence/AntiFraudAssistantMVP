@file:Suppress("ReturnCount")

package com.qalqan.antifraud.web

/**
 * Spec §5.4 / §16.4 — turn user-typed input into a canonical eTLD+1 lowercase string.
 *
 * Pipeline:
 *   1. trim, lowercase
 *   2. strip scheme (anything before "://")
 *   3. strip userinfo (anything before "@")
 *   4. strip path / query / fragment (cut at first '/', '?', '#')
 *   5. strip port (cut at last ':' if remainder is all digits)
 *   6. resolve eTLD+1 via [PublicSuffixSnapshot.longestMatch] (fall back to rightmost two labels)
 *   7. reject if the result is just a public suffix (no registrable label)
 *
 * The §16.4 `WebEvent.init` invariant rejects any string with '/', '?', '#', or "://".
 * Steps 2–5 guarantee none survive.
 */
class DomainNormalizer {
    fun normalize(rawInput: String): NormalizationResult {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return NormalizationResult.Error.Empty

        var s = trimmed.lowercase()
        // strip scheme
        s = s.substringAfter("://", missingDelimiterValue = s)
        // strip userinfo
        s = s.substringAfter('@', missingDelimiterValue = s)
        // strip path/query/fragment
        s = s.substringBefore('/').substringBefore('?').substringBefore('#')
        // strip port (host:digits)
        val colon = s.lastIndexOf(':')
        if (colon >= 0 && s.substring(colon + 1).all(Char::isDigit)) {
            s = s.substring(0, colon)
        }
        if (s.isEmpty() || s.any(Char::isWhitespace)) return NormalizationResult.Error.Invalid(rawInput)
        if ('.' !in s) return NormalizationResult.Error.Invalid(rawInput)

        val canonical =
            resolveEtldPlusOne(s)
                ?: return NormalizationResult.Error.Invalid(rawInput)
        return NormalizationResult.Success(canonical)
    }

    private fun resolveEtldPlusOne(host: String): String? {
        val suffix = PublicSuffixSnapshot.longestMatch(host)
        if (suffix != null) {
            if (host == suffix) return null
            val withoutSuffix = host.removeSuffix(".$suffix")
            val registrableLabel = withoutSuffix.substringAfterLast('.')
            if (registrableLabel.isEmpty()) return null
            return "$registrableLabel.$suffix"
        }
        // Fallback: rightmost two labels.
        val labels = host.split('.').filter { it.isNotEmpty() }
        if (labels.size < 2) return null
        return labels.takeLast(2).joinToString(".")
    }
}
