package com.qalqan.antifraud.database.converters

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Spec §16.6 / §16.7 — RiskSession and RiskCampaign carry collection-typed columns. Persisting
 * them as JSON keeps the Room schema flat without adding join tables.
 */
internal object JsonListConverters {
    private val moshi = Moshi.Builder().build()
    private val stringListAdapter: JsonAdapter<List<String>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, String::class.java))

    @TypeConverter
    @JvmStatic
    fun fromList(list: List<String>?): String? = list?.let(stringListAdapter::toJson)

    @TypeConverter
    @JvmStatic
    fun toList(value: String?): List<String>? = value?.let(stringListAdapter::fromJson)
}
