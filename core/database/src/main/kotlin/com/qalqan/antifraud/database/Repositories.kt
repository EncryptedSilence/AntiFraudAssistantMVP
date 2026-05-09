package com.qalqan.antifraud.database

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.qalqan.antifraud.database.repository.CallEventRepository
import com.qalqan.antifraud.database.repository.ContactProfileRepository
import com.qalqan.antifraud.database.repository.RiskCampaignRepository
import com.qalqan.antifraud.database.repository.RiskSessionRepository
import com.qalqan.antifraud.database.repository.SmsEventRepository
import com.qalqan.antifraud.database.repository.UserAnswerRepository
import com.qalqan.antifraud.database.repository.WebEventRepository

/**
 * Single entry point that holds the [AntifraudDatabase] and exposes one repository per §16 entity.
 * Owners hold this for the lifetime of the process.
 */
class Repositories private constructor(internal val db: AntifraudDatabase) {
    val calls: CallEventRepository = CallEventRepository(db.callEventDao())
    val sms: SmsEventRepository = SmsEventRepository(db.smsEventDao())
    val web: WebEventRepository = WebEventRepository(db.webEventDao())
    val answers: UserAnswerRepository = UserAnswerRepository(db.userAnswerDao())
    val sessions: RiskSessionRepository = RiskSessionRepository(db.riskSessionDao())
    val campaigns: RiskCampaignRepository = RiskCampaignRepository(db.riskCampaignDao())
    val contacts: ContactProfileRepository = ContactProfileRepository(db.contactProfileDao())

    fun close() = db.close()

    companion object {
        fun build(context: Context): Repositories = Repositories(AntifraudDatabase.build(context))

        @VisibleForTesting
        fun inMemory(context: Context): Repositories = Repositories(AntifraudDatabase.inMemory(context))
    }
}
