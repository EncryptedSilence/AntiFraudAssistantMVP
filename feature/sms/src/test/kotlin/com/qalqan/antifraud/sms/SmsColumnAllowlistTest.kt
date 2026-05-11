package com.qalqan.antifraud.sms

import android.provider.Telephony
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import org.junit.Test

/**
 * Privacy boundary: §2.1 / §4.2.2 — we only read the columns required to populate
 * `SmsEvent`. Reading other columns (e.g. STATUS, READ, TYPE, ERROR_CODE) is gratuitous;
 * reading MMS columns (CT_T, CT_L, EXP) crosses §2.1 since MMS may carry media.
 *
 * This test pins `SmsContentProviderReader.PROJECTION`. If a future task adds a column,
 * the spec must justify it.
 */
class SmsColumnAllowlistTest {
    @Test
    fun `projection contains exactly the five allowlisted columns`() {
        SmsContentProviderReader.PROJECTION.toList() shouldContainExactlyInAnyOrder
            listOf(
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.SUBSCRIPTION_ID,
                Telephony.Sms._ID,
            )
    }

    @Test
    fun `projection does not contain forbidden columns`() {
        val proj = SmsContentProviderReader.PROJECTION.toList()
        proj shouldNotContain Telephony.Sms.STATUS
        proj shouldNotContain Telephony.Sms.TYPE
        proj shouldNotContain Telephony.Sms.PROTOCOL
        proj shouldNotContain Telephony.Sms.PERSON
        proj shouldNotContain Telephony.Sms.SERVICE_CENTER
        proj shouldNotContain Telephony.Sms.READ
        proj shouldNotContain Telephony.Sms.ERROR_CODE
    }
}
