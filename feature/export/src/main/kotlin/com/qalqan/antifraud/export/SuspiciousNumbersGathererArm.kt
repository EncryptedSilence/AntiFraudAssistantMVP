package com.qalqan.antifraud.export

import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.ContactProfile
import com.qalqan.antifraud.domain.TrustStatus

/**
 * Spec §8.2 — emits a [ExportRecord.SuspiciousNumber] for every [ContactProfile] with
 * `trustStatus = SUSPICIOUS`. Trusted / neutral / unknown contacts are skipped — the
 * user's "trusted numbers" export is post-MVP.
 *
 * Stage 7 design choice: the gatherer emits `phoneFull = null` unconditionally. The
 * §16.1 `phoneNormalizedEnc` field is a `ByteArray` of CryptoBox-encrypted bytes; we
 * intentionally do NOT decrypt at the gather boundary. The user-facing `phoneLast4`
 * (already non-sensitive per §16.1) is the only phone identifier in Stage 7 exports.
 * When Stage 8 / 9 adds the explicit "include real numbers" toggle (§17.5), it lands a
 * `CryptoBox` injection at this gatherer arm and populates `phoneFull` from there. The
 * [RedactionPipeline]'s `NumbersLast4` redactor is a no-op on the current shape (it
 * already clears a null) — it stays in the option set for the future-full path.
 */
internal object SuspiciousNumbersGathererArm : GathererArm {
    override suspend fun gather(repositories: Repositories): List<ExportRecord> {
        val all = repositories.contacts.listAll()
        return all
            .filter { it.trustStatus == TrustStatus.SUSPICIOUS }
            .map { it.toExportRecord() }
    }

    private fun ContactProfile.toExportRecord(): ExportRecord.SuspiciousNumber =
        ExportRecord.SuspiciousNumber(
            phoneFull = null,
            phoneLast4 = phoneLast4 ?: "????",
            isShortCode = isShortCode,
            displayName = displayNameLocal,
            trustStatus = trustStatus.name.lowercase(),
            firstSeenAt = firstSeenAt,
            riskCounter = riskCounter,
        )
}
