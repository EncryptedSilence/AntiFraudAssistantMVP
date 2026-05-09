package com.qalqan.antifraud.domain

import java.time.Instant

/**
 * Spec §3.1 — RiskEvent is the sum type over the four kinds of observable events handled in Stage 1.
 * Pattern and Manual events surface as the same shapes; manual entries are CallEvent/SmsEvent/WebEvent
 * tagged with `linkedSessionId == null` and a UserAnswer can carry the manual flag in note metadata.
 */
sealed interface RiskEvent {
    val eventId: EventId
    val occurredAt: Instant

    data class Call(val event: CallEvent) : RiskEvent {
        override val eventId: EventId get() = event.id
        override val occurredAt: Instant get() = event.startedAt
    }

    data class Sms(val event: SmsEvent) : RiskEvent {
        override val eventId: EventId get() = event.id
        override val occurredAt: Instant get() = event.receivedAt
    }

    data class Web(val event: WebEvent) : RiskEvent {
        override val eventId: EventId get() = event.id
        override val occurredAt: Instant get() = event.visitedAt
    }

    data class Answer(val event: UserAnswer) : RiskEvent {
        override val eventId: EventId get() = event.relatedEventId
        override val occurredAt: Instant get() = event.createdAt
    }
}
