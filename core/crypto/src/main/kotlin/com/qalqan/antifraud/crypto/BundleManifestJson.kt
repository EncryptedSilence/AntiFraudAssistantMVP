package com.qalqan.antifraud.crypto

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.Instant

/**
 * Spec §7.4 — JSON parsing for `manifest.json` inside an `.afpkg`. Parsing returns
 * [Result.failure] (never throws) on missing fields, wrong types, or any manifest
 * invariant rejected by [BundleManifest.init].
 *
 * The canonical-JSON serializer ([toCanonicalJson]) lives in this file too because the
 * two operations share the Moshi factory.
 */
object BundleManifestJson {
    private val moshi: Moshi = Moshi.Builder()
        .add(InstantJsonAdapter)
        .add(BundlePriorityJsonAdapter)
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val dtoAdapter = moshi.adapter(BundleManifestDto::class.java)

    fun parse(bytes: ByteArray): Result<BundleManifest> = runCatching {
        val dto = dtoAdapter.fromJson(bytes.toString(Charsets.UTF_8))
            ?: error("manifest is null")
        dto.toDomain()
    }

    /**
     * Spec §7.4 — produces the EXACT byte representation that the Ed25519 signature is
     * computed over. Determinism: sorted keys at every level, no whitespace, ISO-8601
     * UTC for `createdAt`. Two semantically equal manifests produce byte-identical output.
     */
    fun toCanonicalJson(manifest: BundleManifest): ByteArray {
        val sortedContents = manifest.contents.toSortedMap()
        val rootMap = linkedMapOf<String, Any?>(
            "contents" to sortedContents,
            "createdAt" to manifest.createdAt.toString(),
            "minAppVersion" to manifest.minAppVersion,
            "previousPackageId" to manifest.previousPackageId,
            "priority" to manifest.priority.name,
            "schemaVersion" to manifest.schemaVersion,
            "source" to manifest.source,
            "version" to manifest.version,
        )
        val sb = StringBuilder()
        writeJson(sb, rootMap)
        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    @Suppress("CyclomaticComplexMethod")
    private fun writeJson(sb: StringBuilder, value: Any?) {
        when (value) {
            null -> sb.append("null")
            is Boolean -> sb.append(value.toString())
            is Number -> sb.append(value.toString())
            is String -> {
                sb.append('"')
                value.forEach { c ->
                    when (c) {
                        '\\' -> sb.append("\\\\")
                        '"' -> sb.append("\\\"")
                        '\n' -> sb.append("\\n")
                        '\r' -> sb.append("\\r")
                        '\t' -> sb.append("\\t")
                        else -> sb.append(c)
                    }
                }
                sb.append('"')
            }
            is Map<*, *> -> {
                sb.append('{')
                val keys = value.keys.map { it.toString() }.sorted()
                keys.forEachIndexed { idx, k ->
                    if (idx > 0) sb.append(',')
                    writeJson(sb, k)
                    sb.append(':')
                    writeJson(sb, value[k])
                }
                sb.append('}')
            }
            else -> error("unsupported value type: ${value::class}")
        }
    }

    @JsonClass(generateAdapter = false)
    internal data class BundleManifestDto(
        val version: String?,
        val createdAt: Instant?,
        val source: String?,
        val schemaVersion: Int?,
        val minAppVersion: Int?,
        val priority: BundlePriority?,
        val previousPackageId: String?,
        val contents: Map<String, String>?,
    ) {
        fun toDomain(): BundleManifest = BundleManifest(
            version = requireNotNull(version) { "version is required" },
            createdAt = requireNotNull(createdAt) { "createdAt is required" },
            source = requireNotNull(source) { "source is required" },
            schemaVersion = requireNotNull(schemaVersion) { "schemaVersion is required" },
            minAppVersion = requireNotNull(minAppVersion) { "minAppVersion is required" },
            priority = requireNotNull(priority) { "priority is required" },
            previousPackageId = previousPackageId,
            contents = requireNotNull(contents) { "contents is required" },
        )
    }
}

internal object InstantJsonAdapter {
    @com.squareup.moshi.FromJson fun fromJson(value: String): Instant = Instant.parse(value)
    @com.squareup.moshi.ToJson fun toJson(value: Instant): String = value.toString()
}

internal object BundlePriorityJsonAdapter {
    @com.squareup.moshi.FromJson fun fromJson(value: String): BundlePriority =
        BundlePriority.valueOf(value.uppercase())
    @com.squareup.moshi.ToJson fun toJson(value: BundlePriority): String = value.name
}
