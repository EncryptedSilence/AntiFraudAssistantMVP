# AntiFraud Assistant — Android MVP

A privacy-respecting Android app that helps users recognize multi-stage social-engineering fraud **before** they share an SMS code, install an attacker's app, transfer money, or open a phishing link.

The app does not use AI of any kind. It does not record audio. It does not read messengers. It does not send user data anywhere by default. It correlates calls, SMS, and other observable signals locally, into multi-day risk campaigns, and intervenes at the moment of risk with a real-time alert.

## Status

Stages 1–3 complete on `main`. Stage 1 (local core) delivered project scaffolding, domain entities, deterministic scoring engine, correlation engine, SQLCipher-encrypted Room database, manual-entry fallback path, application action log, and demo-data import. Stage 2 (pattern engine) added the declarative scenario-pattern engine: typed pattern model, Moshi parser, condition evaluator, AND-of-conditions matcher, ≥3-reasons explainability, pattern state storage (Room schema v2), five in-APK seed patterns, orchestrator integration, and §23 #12 / #14 / #17 acceptance tests. Stage 3 (auto call observer) shipped `:feature:calls`: foreground service of type `phoneCall`, modern (`TelephonyCallback`, API 31+) + legacy (`PhoneStateListener`) listeners, per-`SubscriptionId` multi-SIM registration on Android 12+, `CallLog` reader with five-column allowlisted projection, `CallEventBuilder` reusing the §5.1 normalize-then-salt-hash path shared with `ManualEntry`, `RiskCounterUpdater` for `ContactProfile.riskCounter`, §17.0.3 ongoing transparency notification, permission flow (§4.2.1 runtime pair + `POST_NOTIFICATIONS` on API 33+) and battery-optimization-exemption prompt, with `:app` integration. §23 #25–#28 + §23 #22 (no `RECORD_AUDIO`) + §17.0.3 + §23 #45 + §20.1 all have passing acceptance tests. Stages 4–9 (SMS, web, sync, export, UX, real-time intervention) follow. See [docs/plans/stage1/IMPLEMENTATION_REPORT.md](docs/plans/stage1/IMPLEMENTATION_REPORT.md), [docs/plans/stage2/IMPLEMENTATION_REPORT.md](docs/plans/stage2/IMPLEMENTATION_REPORT.md), and [docs/plans/stage3/IMPLEMENTATION_REPORT.md](docs/plans/stage3/IMPLEMENTATION_REPORT.md) for the per-phase logs.

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
├── .editorconfig
├── .gitignore
├── CLAUDE.md                        (agent-facing project instructions)
├── README.md
├── settings.gradle.kts              (Gradle root + module includes)
├── build.gradle.kts                 (root — clean + checkAll only)
├── gradle.properties
├── gradlew, gradlew.bat
├── gradle/
│   ├── libs.versions.toml           (single source of dependency versions)
│   └── wrapper/
├── build-logic/                     (composite build — convention plugins)
├── config/detekt/                   (detekt rules)
├── app/                             (Android application — Compose status screen, demo importer, pattern-warning surface, §17.7 permission banner)
├── core/
│   ├── domain/                      (JVM — entities per spec §16)
│   ├── scoring/                     (JVM — risk math per §11–12 + Appendix B)
│   ├── correlation/                 (JVM — session + campaign correlators, 14-day horizon, pattern-provider integration)
│   ├── database/                    (Android library — Room v2 + SQLCipher, manual entry, action log, retention purger, pattern_state, CallEntryDigest)
│   ├── demo/                        (Android library — JSON fixture importer for §13 scenarios)
│   └── patterns/                    (JVM — declarative pattern engine: Moshi parser, condition evaluator, matcher, explainer, 5 in-APK seeds)
├── feature/
│   └── calls/                       (Android library — auto call observer: foreground service `phoneCall`, telephony listeners, CallLog reader, multi-SIM, action log)
└── docs/                            (private — gitignored — plans, specs, instructions)
```

(Wiki pages live in a separate repository; see `docs/plans/stage3/WIKI_UPDATES.md` for the Stage 3 updates that need to land there.)

## Building

```bash
./gradlew checkAll              # local quality gate: ktlintCheck + detekt + JVM test + Android testDebugUnitTest
./gradlew assembleDebug         # build the debug APK
./gradlew test                  # JVM unit tests across :core:* modules
./gradlew testDebugUnitTest     # Android unit tests (Robolectric)
./gradlew connectedAndroidTest  # device / emulator tests (none yet — used from Stage 3)
```

Windows-equivalent commands and one-time environment setup live in [docs/instructions/dev-setup-windows.md](docs/instructions/dev-setup-windows.md). Test layers and conventions are described in [docs/instructions/test-instructions.md](docs/instructions/test-instructions.md).

## Distribution

- The MVP ships as a stand-alone APK. Google Play distribution is a separate, later decision and will require additional Play-policy work for the restricted SMS / call-log permissions.

## License

To be decided before the first public release. Until a license file is added, no rights are granted to redistribute or modify the code.

## More information

Wiki pages (architectural overview, glossary, roadmap, and topic pages) live in a separate repository.
