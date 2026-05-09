package com.qalqan.antifraud.database.manual

import android.content.Context
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.crypto.CryptoBox
import com.qalqan.antifraud.database.crypto.KeyStoreCryptoBox
import com.qalqan.antifraud.domain.AnswerCode
import com.qalqan.antifraud.domain.AnswerId
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.DomainHash
import com.qalqan.antifraud.domain.DomainStatus
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.QuestionCode
import com.qalqan.antifraud.domain.SenderHash
import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.domain.SmsEvent
import com.qalqan.antifraud.domain.UserAnswer
import com.qalqan.antifraud.domain.WebEvent
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID

/**
 * Spec §4.1.1 — manual entry is a fallback path. The validation here is the privacy boundary:
 * normalize phones to E.164, hash with a per-install salt, encrypt SMS body excerpts and user notes,
 * reject sensitive content before it reaches the DB.
 */
class ManualEntry private constructor(
    private val repos: Repositories,
    private val phone: PhoneNormalizer,
    private val box: CryptoBox,
    private val salt: ByteArray,
) {
    class SensitiveNoteRejected(message: String) : IllegalArgumentException(message)

    val calls = CallSubmitter()
    val sms = SmsSubmitter()
    val web = WebSubmitter()
    val answers = AnswerSubmitter()

    inner class CallSubmitter {
        suspend fun submit(
            rawNumber: String,
            direction: CallDirection,
            startedAt: Instant,
            durationSec: Long,
            isKnownContact: Boolean,
        ): EventId {
            val n = phone.normalize(rawNumber)
            val hash = Hashing.saltedSha256(n.normalizedE164, salt)
            val id = EventId(UUID.randomUUID().toString())
            val ev =
                CallEvent(
                    id = id,
                    phoneHash = PhoneHash(hash),
                    simSlot = null,
                    direction = direction,
                    startedAt = startedAt,
                    endedAt = startedAt.plusSeconds(durationSec),
                    durationSec = durationSec,
                    isKnownContact = isKnownContact,
                    isRepeated = false,
                    callRiskScore = 0,
                    linkedSessionId = null,
                    linkedCampaignId = null,
                )
            repos.calls.save(ev)
            return id
        }
    }

    inner class SmsSubmitter {
        suspend fun submit(
            sender: String,
            receivedAt: Instant,
            body: String,
            simSlot: Int? = null,
        ): EventId {
            val excerpt = body.take(SmsEvent.MAX_BODY_EXCERPT_CHARS)
            val excerptEnc = box.encrypt(excerpt.toByteArray(Charsets.UTF_8))
            val senderHash = Hashing.saltedSha256(sender.trim(), salt)
            val containsCode = OtpAndIdGuard.isLikelySensitive(body)
            val containsLink = body.contains("http", ignoreCase = true)
            val id = EventId(UUID.randomUUID().toString())
            val ev =
                SmsEvent(
                    id = id,
                    senderHash = SenderHash(senderHash),
                    senderDisplayNameLocal = sender.trim().take(SENDER_DISPLAY_MAX_CHARS),
                    simSlot = simSlot,
                    receivedAt = receivedAt,
                    smsCategory = SmsCategory.UNKNOWN_SENDER,
                    containsCode = containsCode,
                    containsLink = containsLink,
                    containsFinancialKeyword = false,
                    containsSecurityKeyword = false,
                    bodyExcerptEnc = excerptEnc,
                    smsRiskScore = 0,
                    linkedSessionId = null,
                    linkedCampaignId = null,
                )
            repos.sms.save(ev)
            return id
        }
    }

    inner class WebSubmitter {
        suspend fun submit(
            domainEtldPlusOne: String,
            visitedAt: Instant,
        ): EventId {
            val cleaned = domainEtldPlusOne.trim().lowercase()
            require('/' !in cleaned) { "submit only the domain, not a URL" }
            val hash = Hashing.saltedSha256(cleaned, salt)
            val id = EventId(UUID.randomUUID().toString())
            val ev =
                WebEvent(
                    id = id,
                    domainHash = DomainHash(hash),
                    domainDisplayLocal = cleaned,
                    visitedAt = visitedAt,
                    isNewDomain = true,
                    domainStatus = DomainStatus.NEW,
                    webRiskScore = 0,
                    linkedSessionId = null,
                    linkedCampaignId = null,
                )
            repos.web.save(ev)
            return id
        }
    }

    inner class AnswerSubmitter {
        suspend fun submit(
            relatedEventId: EventId,
            questionCode: QuestionCode,
            answerCode: AnswerCode,
            createdAt: Instant,
        ): AnswerId {
            val a = AnswerId(UUID.randomUUID().toString())
            val ans =
                UserAnswer(
                    id = a,
                    relatedEventId = relatedEventId,
                    relatedSessionId = null,
                    relatedCampaignId = null,
                    questionCode = questionCode,
                    answerCode = answerCode,
                    userNoteLocalEnc = null,
                    answerRiskScore = 0,
                    createdAt = createdAt,
                )
            repos.answers.save(ans)
            return a
        }

        suspend fun submitNote(
            relatedEventId: EventId,
            noteText: String,
        ): AnswerId {
            if (OtpAndIdGuard.isLikelySensitive(noteText)) {
                throw SensitiveNoteRejected(
                    "Note appears to contain an OTP / ID / card number; it cannot be saved.",
                )
            }
            val truncated = noteText.take(UserAnswer.MAX_NOTE_CHARS)
            val enc = box.encrypt(truncated.toByteArray(Charsets.UTF_8))
            val a = AnswerId(UUID.randomUUID().toString())
            val ans =
                UserAnswer(
                    id = a,
                    relatedEventId = relatedEventId,
                    relatedSessionId = null,
                    relatedCampaignId = null,
                    questionCode = QuestionCode.Q1_CALLER_OFFICIAL_CLAIM,
                    answerCode = AnswerCode.NOT_ANSWERED,
                    userNoteLocalEnc = enc,
                    answerRiskScore = 0,
                    createdAt = Instant.now(),
                )
            repos.answers.save(ans)
            return a
        }
    }

    companion object {
        private const val SALT_FILE = "antifraud.hash.salt"
        private const val SALT_BYTES = 16
        private const val DEFAULT_COUNTRY_CODE = 7
        private const val SENDER_DISPLAY_MAX_CHARS = 80

        fun create(
            context: Context,
            repos: Repositories,
        ): ManualEntry {
            val box = KeyStoreCryptoBox.create(context, alias = "antifraud.field_box")
            return create(context, repos, box)
        }

        /**
         * Test-friendly factory: callers pass an explicit [CryptoBox] (e.g. `InMemoryCryptoBox`)
         * so unit tests can run under Robolectric, where AndroidKeyStore is unavailable.
         */
        fun create(
            context: Context,
            repos: Repositories,
            box: CryptoBox,
        ): ManualEntry {
            val salt = obtainSalt(context, box)
            return ManualEntry(
                repos = repos,
                phone = PhoneNormalizer(defaultCountryCode = DEFAULT_COUNTRY_CODE),
                box = box,
                salt = salt,
            )
        }

        private fun obtainSalt(
            context: Context,
            box: CryptoBox,
        ): ByteArray {
            val file = java.io.File(context.filesDir, SALT_FILE)
            if (file.exists()) return box.decrypt(file.readBytes())
            val raw = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
            file.writeBytes(box.encrypt(raw))
            return raw
        }
    }
}
