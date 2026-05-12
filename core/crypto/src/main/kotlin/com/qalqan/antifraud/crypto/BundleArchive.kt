package com.qalqan.antifraud.crypto

/**
 * Spec §7.4 — in-memory contents of a verified-or-not `.afpkg`. Kotlin's auto-generated
 * `equals` / `hashCode` for data classes compares `ByteArray` by reference, which makes
 * unit tests painful; we override both to compare bytes by content.
 */
class BundleArchive(
    val manifestBytes: ByteArray,
    val signature: ByteArray,
    val dataEntries: Map<String, ByteArray>,
) {
    init {
        require(signature.size == ED25519_SIGNATURE_BYTES) {
            "signature must be exactly $ED25519_SIGNATURE_BYTES bytes, was ${signature.size}"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BundleArchive) return false
        if (!manifestBytes.contentEquals(other.manifestBytes)) return false
        if (!signature.contentEquals(other.signature)) return false
        if (dataEntries.keys != other.dataEntries.keys) return false
        return dataEntries.all { (k, v) -> v.contentEquals(other.dataEntries[k]) }
    }

    override fun hashCode(): Int {
        var result = manifestBytes.contentHashCode()
        result = 31 * result + signature.contentHashCode()
        result = 31 * result + dataEntries.entries.sumOf { (k, v) ->
            k.hashCode() xor v.contentHashCode()
        }
        return result
    }

    companion object {
        const val ED25519_SIGNATURE_BYTES = 64
    }
}
