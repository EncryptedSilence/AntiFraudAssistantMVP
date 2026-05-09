package com.qalqan.antifraud.domain

@JvmInline
value class PhoneHash(val value: String) {
    init {
        require(value.isNotBlank()) { "PhoneHash must not be blank" }
    }
}

@JvmInline
value class DomainHash(val value: String) {
    init {
        require(value.isNotBlank()) { "DomainHash must not be blank" }
    }
}

@JvmInline
value class SenderHash(val value: String) {
    init {
        require(value.isNotBlank()) { "SenderHash must not be blank" }
    }
}

@JvmInline
value class EventId(val value: String) {
    init {
        require(value.isNotBlank()) { "EventId must not be blank" }
    }
}

@JvmInline
value class SessionId(val value: String) {
    init {
        require(value.isNotBlank()) { "SessionId must not be blank" }
    }
}

@JvmInline
value class CampaignId(val value: String) {
    init {
        require(value.isNotBlank()) { "CampaignId must not be blank" }
    }
}

@JvmInline
value class PatternId(val value: String) {
    init {
        require(value.isNotBlank()) { "PatternId must not be blank" }
    }
}

@JvmInline
value class AnswerId(val value: String) {
    init {
        require(value.isNotBlank()) { "AnswerId must not be blank" }
    }
}
