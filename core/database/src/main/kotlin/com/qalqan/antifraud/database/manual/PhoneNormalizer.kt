package com.qalqan.antifraud.database.manual

/**
 * Spec §5.1 — produces E.164 form for matching, last4 for display, and isShortCode for §12 scoring.
 *
 * MVP scope: Kazakhstan-flavored. Other locales are post-MVP per §24 #11.
 */
class PhoneNormalizer(private val defaultCountryCode: Int) {
    data class Result(
        val normalizedE164: String,
        val last4: String?,
        val isShortCode: Boolean,
    )

    fun normalize(raw: String): Result {
        require(raw.isNotBlank()) { "phone must not be blank" }
        val digits = raw.filter(Char::isDigit)
        val plusPrefixed = raw.trim().startsWith("+")
        require(digits.isNotEmpty()) { "phone has no digits" }

        // Short code: 3-7 digits, no '+' prefix, no leading "8"
        if (!plusPrefixed && digits.length in 3..7) {
            return Result(normalizedE164 = digits, last4 = null, isShortCode = true)
        }

        val withCountry =
            when {
                plusPrefixed -> digits
                digits.startsWith("8") && digits.length == LEADING_8_LEN ->
                    "$defaultCountryCode${digits.drop(1)}"
                digits.length == LOCAL_NUMBER_LEN -> "$defaultCountryCode$digits"
                digits.startsWith(defaultCountryCode.toString()) -> digits
                else -> digits
            }
        val last4 = withCountry.takeLast(4).takeIf { it.length == 4 && it.all(Char::isDigit) }
        return Result(
            normalizedE164 = "+$withCountry",
            last4 = last4,
            isShortCode = false,
        )
    }

    private companion object {
        const val LEADING_8_LEN = 11
        const val LOCAL_NUMBER_LEN = 10
    }
}
