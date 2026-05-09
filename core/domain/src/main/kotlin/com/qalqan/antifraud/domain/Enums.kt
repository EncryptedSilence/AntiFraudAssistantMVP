package com.qalqan.antifraud.domain

enum class TrustStatus { TRUSTED, NEUTRAL, SUSPICIOUS, BLOCKED, UNKNOWN }

enum class DomainStatus { NEW, KNOWN, TRUSTED, SUSPICIOUS, BLOCKED, IGNORED }

enum class CallDirection { INCOMING, OUTGOING, MISSED }

enum class SessionStatus { OPEN, CLOSED_AUTO, CLOSED_BY_USER, ARCHIVED, FALSE_POSITIVE }

enum class CampaignStatus { ACTIVE, CLOSED, ARCHIVED, FALSE_POSITIVE }

enum class SmsCategory {
    BANK,
    AUTHORITY_SHORTCODE,
    SERVICE,
    UNKNOWN_SENDER,
    OTP,
    LINK,
    LOGIN,
    REGISTRATION,
    PASSWORD_CHANGE,
    TRANSFER,
    LOAN,
    SECURITY_WARNING
}

enum class ScenarioCategory {
    BANK_FRAUD,
    AUTHORITY_SPOOF,
    INVESTMENT_SCHEME,
    DELIVERY_SCAM,
    TECH_SUPPORT_SCAM,
    UNKNOWN_SOCIAL_ENGINEERING
}

enum class RiskBand { LOW, MEDIUM, HIGH, CRITICAL }
