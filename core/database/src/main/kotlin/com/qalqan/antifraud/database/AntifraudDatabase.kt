package com.qalqan.antifraud.database

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.qalqan.antifraud.database.answers.UserAnswerDao
import com.qalqan.antifraud.database.answers.UserAnswerEntity
import com.qalqan.antifraud.database.calls.CallEventDao
import com.qalqan.antifraud.database.calls.CallEventEntity
import com.qalqan.antifraud.database.campaigns.RiskCampaignDao
import com.qalqan.antifraud.database.campaigns.RiskCampaignEntity
import com.qalqan.antifraud.database.contacts.ContactProfileDao
import com.qalqan.antifraud.database.contacts.ContactProfileEntity
import com.qalqan.antifraud.database.converters.JsonListConverters
import com.qalqan.antifraud.database.crypto.DatabaseKeyProvider
import com.qalqan.antifraud.database.sessions.RiskSessionDao
import com.qalqan.antifraud.database.sessions.RiskSessionEntity
import com.qalqan.antifraud.database.sms.SmsEventDao
import com.qalqan.antifraud.database.sms.SmsEventEntity
import com.qalqan.antifraud.database.web.WebEventDao
import com.qalqan.antifraud.database.web.WebEventEntity

@Database(
    entities = [
        ContactProfileEntity::class,
        CallEventEntity::class,
        SmsEventEntity::class,
        WebEventEntity::class,
        UserAnswerEntity::class,
        RiskSessionEntity::class,
        RiskCampaignEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(JsonListConverters::class)
abstract class AntifraudDatabase : RoomDatabase() {
    internal abstract fun contactProfileDao(): ContactProfileDao

    internal abstract fun callEventDao(): CallEventDao

    internal abstract fun smsEventDao(): SmsEventDao

    internal abstract fun webEventDao(): WebEventDao

    internal abstract fun userAnswerDao(): UserAnswerDao

    internal abstract fun riskSessionDao(): RiskSessionDao

    internal abstract fun riskCampaignDao(): RiskCampaignDao

    companion object {
        private const val NAME = "antifraud.db"

        /**
         * Production factory — opens an encrypted SQLCipher database with a Keystore-wrapped key.
         * Spec §15.1.
         */
        fun build(context: Context): AntifraudDatabase {
            val keyProvider = DatabaseKeyProvider.fromContext(context)
            return Room.databaseBuilder(context, AntifraudDatabase::class.java, NAME)
                .openHelperFactory(sqlCipherFactory(keyProvider))
                .fallbackToDestructiveMigrationOnDowngrade(true)
                .build()
        }

        /**
         * Test-only factory — opens an unencrypted in-memory Room database. Avoids loading the
         * SQLCipher native library, which is unavailable under JVM unit tests / Robolectric.
         */
        @VisibleForTesting
        fun inMemory(context: Context): AntifraudDatabase =
            Room.inMemoryDatabaseBuilder(context, AntifraudDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }
}
