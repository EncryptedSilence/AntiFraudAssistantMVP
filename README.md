# AntiFraud Assistant — Android MVP

A privacy-respecting Android app that helps users recognize multi-stage social-engineering fraud **before** they share an SMS code, install an attacker's app, transfer money, or open a phishing link.

The app does not use AI of any kind. It does not record audio. It does not read messengers. It does not send user data anywhere by default. It correlates calls, SMS, and other observable signals locally, into multi-day risk campaigns, and intervenes at the moment of risk with a real-time alert.

## Status

Pre-implementation. The repository will be populated with the Kotlin / Jetpack Compose codebase as development progresses. The detailed technical specification is currently kept off the public repo.

## What it does

- Captures incoming, outgoing, and missed calls automatically — no default-Phone role required.
- Captures incoming SMS automatically — no default-SMS role required.
- Correlates events into short-window risk sessions and multi-day risk campaigns.
- Matches against a local catalog of fraud-scenario patterns (data, not code).
- Delivers a real-time alert (full-screen notification or overlay banner) at the moment risk crosses the critical threshold.
- Lets the user maintain trusted / suspicious phone-number and domain lists locally.
- Lets the user export selected data manually, with an explicit redaction preview.

## What it never does

- No cloud or on-device AI.
- No audio recording, no speech recognition, no voice analysis.
- No reading of messenger conversations.
- No accessibility-service abuse.
- No background upload of contacts, calls, SMS, domains, or risk history.
- No advertising or tracking SDKs.
- No legal verdict — the app says "possible fraud", never "fraud confirmed".
- No call blocking, no SMS provider writes, no banking-app interference.

## Core architecture

| Pillar | What it means |
|---|---|
| **Passive observation** | Calls and SMS are captured automatically using standard Android runtime permissions (`READ_PHONE_STATE`, `READ_CALL_LOG`, `RECEIVE_SMS`, `READ_SMS`). Manual entry exists only as a permission-denied fallback. |
| **Real-time intervention** | When risk reaches the critical threshold, the app fires a full-screen notification with full-screen-intent (Android `CATEGORY_CALL`); when granted, an overlay banner backs that up if the user is in another foreground app. |
| **Local-only privacy** | All data lives on-device in an encrypted Room + SQLCipher database with keys in Android Keystore. Sync is download-only and user-toggled. |
| **Declarative fraud patterns** | Scenario rules are JSON Schema documents, not executable code. They can be updated independently from the app, signed, validated, enabled / disabled, and locally curated. |

## Planned tech stack

- Kotlin
- Jetpack Compose
- Room + SQLCipher
- Android Keystore
- WorkManager + foreground service (`FOREGROUND_SERVICE_PHONE_CALL`)
- Notification full-screen intent (`USE_FULL_SCREEN_INTENT`) and `SYSTEM_ALERT_WINDOW`
- Ed25519 signatures + SHA-256 checksums for update packages
- HTTPS download-only update client

## Repository layout (today)

```
.
├── .gitignore
├── README.md
└── wiki/
    └── Home.md
```

The `docs/` directory holding the technical specification is intentionally excluded from this public repository while the spec is in active revision. Once stabilized, the relevant parts will be promoted into the GitHub wiki.

## Building

The Gradle project will be added in a later commit. Once present, the canonical build commands will be:

```bash
./gradlew assembleDebug         # build the debug APK
./gradlew test                  # JVM unit tests
./gradlew connectedAndroidTest  # device / emulator tests
./gradlew lint ktlintCheck detekt
```

Until the codebase lands, this section is a placeholder.

## Distribution

- The MVP ships as a stand-alone APK. Google Play distribution is a separate, later decision and will require additional Play-policy work for the restricted SMS / call-log permissions.

## License

To be decided before the first public release. Until a license file is added, no rights are granted to redistribute or modify the code.

## More information

See the [project wiki](https://github.com/EncryptedSilence/AntiFraudAssistantMVP/wiki) (or `wiki/Home.md` in this repository) for an architectural overview, glossary, and roadmap.
