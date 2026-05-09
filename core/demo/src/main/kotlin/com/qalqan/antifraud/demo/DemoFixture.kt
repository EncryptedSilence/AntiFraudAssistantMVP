package com.qalqan.antifraud.demo

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.Instant

sealed interface DemoEvent {
    val offsetSeconds: Long

    @JsonClass(generateAdapter = false)
    data class Call(
        val rawNumber: String,
        val direction: String,
        override val offsetSeconds: Long,
        val durationSec: Long,
        val isKnownContact: Boolean,
    ) : DemoEvent

    @JsonClass(generateAdapter = false)
    data class Sms(
        val sender: String,
        override val offsetSeconds: Long,
        val body: String,
    ) : DemoEvent

    @JsonClass(generateAdapter = false)
    data class Web(
        val domain: String,
        override val offsetSeconds: Long,
    ) : DemoEvent
}

data class DemoFixture(
    val name: String,
    val specReference: String,
    val anchorAt: Instant,
    val events: List<DemoEvent>,
) {
    companion object {
        private val moshi: Moshi =
            Moshi.Builder()
                .add(DemoEventAdapter())
                .add(InstantAdapter())
                .add(KotlinJsonAdapterFactory())
                .build()

        private val adapter: JsonAdapter<DemoFixture> = moshi.adapter(DemoFixture::class.java)

        fun fromJson(json: String): DemoFixture = requireNotNull(adapter.fromJson(json)) { PARSE_FAILURE_MESSAGE }

        private const val PARSE_FAILURE_MESSAGE = "could not parse demo fixture"
    }
}

private class InstantAdapter {
    @com.squareup.moshi.FromJson fun fromJson(value: String): Instant = Instant.parse(value)

    @com.squareup.moshi.ToJson fun toJson(value: Instant): String = value.toString()
}

private class DemoEventAdapter {
    @com.squareup.moshi.FromJson
    fun fromJson(reader: JsonReader): DemoEvent {
        val raw =
            reader.readJsonValue() as? Map<*, *>
                ?: error("expected JSON object")
        val type = raw["type"] as? String ?: error("missing type")
        return when (type) {
            "Call" ->
                DemoEvent.Call(
                    rawNumber = raw["rawNumber"] as String,
                    direction = raw["direction"] as String,
                    offsetSeconds = (raw["offsetSeconds"] as Number).toLong(),
                    durationSec = (raw["durationSec"] as Number).toLong(),
                    isKnownContact = raw["isKnownContact"] as Boolean,
                )
            "Sms" ->
                DemoEvent.Sms(
                    sender = raw["sender"] as String,
                    offsetSeconds = (raw["offsetSeconds"] as Number).toLong(),
                    body = raw["body"] as String,
                )
            "Web" ->
                DemoEvent.Web(
                    domain = raw["domain"] as String,
                    offsetSeconds = (raw["offsetSeconds"] as Number).toLong(),
                )
            else -> error("unknown event type: $type")
        }
    }

    @com.squareup.moshi.ToJson
    fun toJson(
        writer: JsonWriter,
        event: DemoEvent,
    ) {
        writer.beginObject()
        when (event) {
            is DemoEvent.Call -> {
                writer.name("type").value("Call")
                writer.name("rawNumber").value(event.rawNumber)
                writer.name("direction").value(event.direction)
                writer.name("offsetSeconds").value(event.offsetSeconds)
                writer.name("durationSec").value(event.durationSec)
                writer.name("isKnownContact").value(event.isKnownContact)
            }
            is DemoEvent.Sms -> {
                writer.name("type").value("Sms")
                writer.name("sender").value(event.sender)
                writer.name("offsetSeconds").value(event.offsetSeconds)
                writer.name("body").value(event.body)
            }
            is DemoEvent.Web -> {
                writer.name("type").value("Web")
                writer.name("domain").value(event.domain)
                writer.name("offsetSeconds").value(event.offsetSeconds)
            }
        }
        writer.endObject()
    }
}
