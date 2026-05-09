package com.qalqan.antifraud.scoring

import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.domain.SmsEvent

/**
 * Spec §12.2.
 */
object SmsBaseRisk {
    fun compute(sms: SmsEvent): Int {
        var score = 0
        if (sms.smsCategory == SmsCategory.UNKNOWN_SENDER) score += 10
        if (sms.containsLink) score += 20
        if (sms.containsCode) score += 30
        if (sms.smsCategory == SmsCategory.BANK) score += 20
        if (sms.smsCategory == SmsCategory.AUTHORITY_SHORTCODE) score += 20
        if (sms.smsCategory == SmsCategory.LOGIN) score += 30
        if (sms.smsCategory == SmsCategory.REGISTRATION) score += 30
        if (sms.smsCategory == SmsCategory.PASSWORD_CHANGE) score += 30
        return score
    }
}
