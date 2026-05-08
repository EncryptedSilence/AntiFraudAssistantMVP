# AntiFraud Assistant — Wiki

This is the entry point for project documentation. The repository's [README](../README.md) covers the elevator pitch and the published surface area; this wiki is for architecture, design rationale, and the moving parts a contributor or reviewer needs to navigate the codebase.

> The detailed technical specification is currently kept private while it is in active revision. Stable sections will be migrated here as they settle.

## What this project is

A multi-stage fraud is rarely a single bad call or a single phishing SMS. It is a sequence: an unknown call with no immediate ask, then an SMS with a code two minutes later, then another call referencing that SMS, then a "support agent" asking the user to install an app or read a code aloud. By the time an individual step looks alarming, the user is already three steps into the funnel.

This Android app builds a local memory of those steps and intervenes at the moment one of them becomes dangerous. It does so without AI, without recording anything, and without sending the user's data anywhere by default.

## Three pillars

### 1. Passive observation

Real users will not paste their calls or SMS into a fraud-detection app. Manual entry is dead weight in production. The app observes the relevant signals automatically, using standard Android runtime permissions, and never asks the user to log anything as a primary flow.

| Signal | Mechanism | Permission set |
|---|---|---|
| Calls (incoming, outgoing, missed) | `TelephonyCallback.CallStateListener` + call-log read on idle | `READ_PHONE_STATE`, `READ_CALL_LOG`, `FOREGROUND_SERVICE_PHONE_CALL` |
| SMS (incoming) | `BroadcastReceiver` for `SMS_RECEIVED_ACTION`, plus a content-provider sweep to recover broadcasts missed during app standby | `RECEIVE_SMS`, `READ_SMS` |
| New domains | Decision currently open: VPN-based DNS sinkhole, accessibility-based address-bar reading, or accepted blind spot. Tracked separately. | t.b.d. |

What the app explicitly does **not** do:

- request the default-Phone, default-Call-Screening, or default-SMS roles;
- record or analyze call audio;
- read messenger conversations or use the accessibility service for that purpose;
- store full URLs, query parameters, cookies, logins, passwords, or banking data.

### 2. Real-time intervention

Observation without intervention has the same failure mode as manual entry: the user finds out too late. The app therefore treats the alert surface as a first-class part of the architecture, not as a notification afterthought.

- **Primary surface** — a full-screen notification with a full-screen intent (`USE_FULL_SCREEN_INTENT`) on a dedicated channel marked `CATEGORY_CALL`. The OS treats it with the same priority as an incoming call, which is what is needed: the user must be interrupted while the risky action is happening.
- **Secondary surface** — a `SYSTEM_ALERT_WINDOW` overlay banner, fired only at the critical-risk level and only when the user is currently in a relevant foreground app (a banking app, a browser on a known-fraud lookalike domain). Detected via `UsageStatsManager`.
- **Passive transparency** — the always-running observer foreground service shows a low-priority ongoing notification (`Watching for fraud signals — last 24 h: N events, M alerts`) so the user can see at a glance what the app is doing and why it has the permissions it has.

Latency budgets are concrete and tested:

| Trigger | Target |
|---|---:|
| Critical-risk decision during an active call | < 2 s after the score crosses 80 |
| Critical-risk decision triggered by SMS arrival | < 3 s after the broadcast |
| High-risk decision on call IDLE or SMS arrival | < 5 s |
| Medium / low | no notification with sound or vibration |

### 3. Local-only privacy

- All data lives on-device in an encrypted Room + SQLCipher database, with keys held in Android Keystore.
- Phone numbers and domains are stored as a salted-hash pair plus an encrypted plaintext display value. Display works; comparison and export-anonymization both work without leaking the original.
- SMS bodies are stored truncated to 200 characters, encrypted, and clearable per-message.
- Sync is download-only and user-toggled. Update packages are signed (Ed25519, single embedded public key) and rejected if signature, schema version, or checksum is wrong.
- Export is always manual, always shows a redaction preview before writing the file, and never includes data the user did not see in that preview.

## Risk model — at a glance

The app uses a deliberately simple, deterministic scoring model. There is no AI, no heuristic that requires explanation. The contributing scores are auditable and the cap behavior is explicit.

```
EventRisk          = min(100, BaseRisk + ContextRisk + UserAnswerRisk)

SessionRisk        = round(0.35*CallRisk + 0.30*SmsRisk + 0.20*WebRisk + 0.15*AnswerRisk)

CampaignRiskScore  = round(min(100,
                       sum_i(EventRisk_i * TimeDecay_i * LinkStrength_i)
                       + PatternRisk))
```

| Score | Level | App action |
|---:|---|---|
| 0–30 | low | log only |
| 31–60 | medium | soft notification |
| 61–80 | high | full-screen alert + at most three yes/no questions per campaign |
| 81–100 | critical | full-screen alert + overlay banner if granted; "pause before action" modal |

`UserAnswerRisk` only contributes via the event the answer is attached to — it is not double-counted at the campaign level. `PatternRisk` is the aggregate weight of triggered scenario patterns, capped at 60 per campaign.

## Scenario patterns

Fraud scenarios are not hard-coded. Each is a JSON document validated against a public schema (`ScenarioPattern`). A pattern declares:

- the kinds of events it cares about (`CallEvent`, `SmsEvent`, `WebEvent`, `UserAnswerEvent`, ...);
- per-condition fields, operators, allowed values, weights, and time windows;
- a correlation block (max campaign age, link strength);
- a warning level + title + message;
- versioning, source, signature status.

Patterns can be enabled or disabled per-pattern, reset to defaults, updated through the signed `stable` channel, or imported from a file (`local` channel). User-created patterns are a power-user feature behind a Settings toggle and are not part of the MVP.

## Module layout (planned)

```
:app                 entry, navigation, onboarding, DI
:core:database       Room + SQLCipher, encrypted entities
:core:crypto         Keystore, Ed25519 verify, SHA-256
:core:scoring        risk model, no Android dependencies (fast unit tests)
:core:patterns       JSON Schema validator, catalog, signature verification
:feature:calls       call observer (TelephonyCallback + foreground service)
:feature:sms         SMS broadcast receiver + content-provider sweep
:feature:web         web-channel module (decision pending)
:feature:campaigns   RiskSession / RiskCampaign correlation
:feature:alerts      full-screen intent activity, overlay banner, ongoing notification
:feature:export      redaction preview + writers (TXT / Markdown / JSON / CSV)
:feature:settings    sensitivity, retention, permission state
:feature:patterns    system-pattern enable / disable / reset
```

This layout will be revised when the Gradle project lands; the wiki page will be updated to match.

## Roadmap

The MVP scope is intentionally narrow. Features marked **post-MVP** are valuable but deferred to keep the demo focused.

### MVP

- automatic call capture (no default-Phone role)
- automatic SMS capture (no default-SMS role)
- local risk-session and risk-campaign correlation
- shipped + signed scenario-pattern catalog with at least five seed patterns
- real-time intervention surface (full-screen notification + overlay banner + ongoing notification)
- pause-before-dangerous-action modal at critical risk
- educational cards (5)
- lookalike-domain Levenshtein check against ~20 known KZ banks and services
- redaction-preview-mandatory export to TXT / Markdown / JSON / CSV
- demo data import for pilot demonstrations

### Post-MVP

- web-channel detection (decision pending: VpnService vs accessibility vs documented blind spot)
- default-SMS role for SMS-provider writes (only if the product later needs to write to the inbox)
- default-Phone or default-Call-Screening roles (only if blocking / pre-screening is added)
- SMS format-authenticity module (signed local catalog of canonical bank / authority SMS templates, format-deviation scoring without AI)
- user-pattern wizard (gated behind a Settings "Enable advanced rules" toggle)
- enterprise / authority bundle channel
- formal export templates for institutions
- simplified UI for elderly users
- encrypted local backup
- family mode

## Glossary

- **RiskEvent** — a single observed occurrence (call, SMS, web visit, user answer, manual entry, pattern trigger).
- **RiskSession** — a short-window grouping of related events (5 min – 24 h).
- **RiskCampaign** — a multi-day grouping of related events with a 14-day active correlation horizon.
- **EventRisk** — a single event's score, capped at 100, combining base, contextual, and user-answer signals.
- **CampaignRiskScore** — the aggregated 0–100 score for a campaign, applied against the four risk levels above.
- **LinkStrength** — coefficient (0–1) describing how strongly two events are correlated.
- **TimeDecay** — coefficient (0–1) reducing the contribution of older events.
- **ScenarioPattern** — a declarative JSON document describing a fraud scheme. Cannot contain executable code.
- **System pattern** — pattern shipped with the app or via a signed update.
- **User pattern** — pattern created locally by the user (post-MVP).
- **`stable` channel** — pattern / reference data downloaded from the trusted single signed source.
- **`local` channel** — pattern / reference data the user imports from a file.
- **MVP** — the demo deliverable scoped in this project; not the production / Google Play release.
- **Active correlation horizon** — 14 days. Events older than this do not affect current risk, although they may still be in the 30-day archive.

## Where to next

Future wiki pages (to be created as the codebase lands and concepts stabilize):

- *Permissions and onboarding* — what each permission unlocks, what is requested when, what happens on denial.
- *Privacy boundaries* — the rules that must hold, the tests that prove they hold.
- *Patterns and the catalog* — how to author and validate a `ScenarioPattern`.
- *Update packages and signing* — package format, signature verification, rollback.
- *Real-time alert surface* — channel configuration, full-screen-intent activity, overlay banner.
- *Acceptance test plan* — the testable acceptance criteria mapped to automated and manual checks.
- *Locale and normalization* — phone normalization, short codes, language extraction.
