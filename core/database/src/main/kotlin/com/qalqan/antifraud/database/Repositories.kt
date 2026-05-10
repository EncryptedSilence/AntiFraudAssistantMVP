package com.qalqan.antifraud.database

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.qalqan.antifraud.database.crypto.DatabaseKeyProvider
import com.qalqan.antifraud.database.log.ApplicationActionLogRepository
import com.qalqan.antifraud.database.log.ApplicationActionLogger
import com.qalqan.antifraud.database.patterns.PatternStateRepository
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
class Repositories private constructor(
    internal val db: AntifraudDatabase,
    private val onWipe: () -> Unit,
) {
    val calls: CallEventRepository = CallEventRepository(db.callEventDao())
    val sms: SmsEventRepository = SmsEventRepository(db.smsEventDao())
    val web: WebEventRepository = WebEventRepository(db.webEventDao())
    val answers: UserAnswerRepository = UserAnswerRepository(db.userAnswerDao())
    val sessions: RiskSessionRepository = RiskSessionRepository(db.riskSessionDao())
    val campaigns: RiskCampaignRepository = RiskCampaignRepository(db.riskCampaignDao())
    val contacts: ContactProfileRepository = ContactProfileRepository(db.contactProfileDao())
    val actionLog: ApplicationActionLogRepository = ApplicationActionLogRepository(db.applicationActionLogDao())
    val actionLogger: ApplicationActionLogger = ApplicationActionLogger(db.applicationActionLogDao())
    val patternState: PatternStateRepository = PatternStateRepository(db.patternStateDao())

    /**
     * Spec §23 #20 — full local wipe. Deletes every row across all entity tables, then resets the
     * SQLCipher database key (production) so the next open generates a new one.
     */
    suspend fun wipeAll() {
        db.callEventDao().deleteAll()
        db.smsEventDao().deleteAll()
        db.webEventDao().deleteAll()
        db.userAnswerDao().deleteAll()
        db.riskSessionDao().deleteAll()
        db.riskCampaignDao().deleteAll()
        db.contactProfileDao().deleteAll()
        db.applicationActionLogDao().deleteAll()
        onWipe()
    }

    fun close() = db.close()

    companion object {
        fun build(context: Context): Repositories {
            val keyProvider = DatabaseKeyProvider.fromContext(context)
            val db = AntifraudDatabase.build(context, keyProvider)
            return Repositories(db) { keyProvider.deleteKey() }
        }

        @VisibleForTesting
        fun inMemory(context: Context): Repositories =
            Repositories(AntifraudDatabase.inMemory(context)) { /* no on-disk key for in-memory */ }
    }
}
