# Real-time alert surface — **stub, lands with Stage 9**

> Stage status: **planned.** Spec §4.4 + §17.0 are authoritative. This page collects the pinned decisions and flags the items that need real-device measurement before they can be promised.

## Why this is a first-class subsystem

Observation without intervention is the same failure mode as manual entry: the user finds out too late. A push notification the user can swipe away during an active phishing call doesn't move the needle. The app needs a surface that interrupts the user *while* the dangerous action is happening, with the same OS priority as an incoming call.

## Pinned decisions (from §4.4 and §24)

- **Primary surface** — full-screen notification with `USE_FULL_SCREEN_INTENT`, on a dedicated channel marked `CATEGORY_CALL` and `IMPORTANCE_HIGH`. The OS treats it with the same interrupt priority as an incoming call.
- **Secondary surface** — `SYSTEM_ALERT_WINDOW` overlay banner, fired only at the **critical** level and only when the user is currently in a foreground app that matters (a banking app, or a browser on a known-fraud lookalike domain). Detected via `UsageStatsManager`.
- **Passive transparency** — the always-running observer foreground service shows a low-priority ongoing notification (for example, *"Watching for fraud signals — last 24 h: N events, M alerts"*) so the user can see at a glance what the app is doing and why it has the permissions it has.
- **No sound or vibration on medium / low.** Only high and critical levels get the interruptive surface.
- **No call blocking, no SMS provider writes, no banking-app interference.** The intervention is a UI surface, not a control.

## Latency targets (§4.4.2)

These come from §4.4.2 and are **mandatory**, not aspirational. They will be tested on real devices in Stage 9 — `superpowers:verification-before-completion` mandates "I observed a < 2 s alert on a real device", not "I implemented the full-screen intent".

| Trigger | Target |
|---|---:|
| Critical-risk decision during an active call | < 2 s after the score crosses 80 |
| Critical-risk decision triggered by SMS arrival | < 3 s after the broadcast |
| High-risk decision on call IDLE or SMS arrival | < 5 s |
| Medium / low | no notification with sound or vibration |

## Open questions (Stage 9 work)

- **OEM compatibility matrix.** Samsung, Xiaomi/MIUI, Huawei without GMS, and stock Pixel each have different rules around full-screen intents on locked screens, background-start restrictions, and overlay-permission UX. The matrix needs concrete observations, not assumptions. Each row needs to specify: does FSI fire when locked? does FSI fire from background? what happens when the OEM "battery saver" is on?
- **Channel re-creation when the user blocks it.** If a user disables the notification channel, the channel cannot be re-created with the same id. We need a strategy: detect the disabled state and surface a rationale screen?
- **Overlay banner UX.** When `SYSTEM_ALERT_WINDOW` is granted but the foreground app is something the user is *intentionally* using (e.g. an actual call), the banner must not block critical UI. Layout, dismiss behavior, accessibility are all open.
- **Foreground service type.** Android 14+ requires `FOREGROUND_SERVICE_PHONE_CALL` for the call-observer service and `FOREGROUND_SERVICE_DATA_SYNC` for the alert/sync service. Both must be declared and the ongoing-notification copy must accurately advertise them.
- **`POST_NOTIFICATIONS` denial.** On Android 13+ this is runtime. If the user denies it, the app loses the primary surface — what does the app do? Probably: degrade to overlay-only at critical level, surface a rationale. Concrete copy is open.
- **Latency measurement methodology.** "Score crosses 80" → "FSI activity rendered" needs an instrumentation harness on a real device. This is part of the Stage 9 acceptance gate, not a unit test.

## What lands when

- **Stage 1** — risk scoring + correlation produce the trigger condition. Done.
- **Stage 2** — pattern matching contributes triggered weights to `CampaignRiskScorer`. Done.
- **Stage 3** — call observer foreground service. The ongoing notification copy is part of that ticket.
- **Stage 9** — the alert surface itself: notification channel, full-screen-intent activity, overlay banner, OEM matrix testing, latency measurement.

## Out of scope for this page

- Pre-call screening / call rejection. Spec §2.1 forbids it. The app surfaces information; it does not block.
- Notification copy localization. §24 #11 — Russian primary, KZ + EN added post-MVP.
- A11y service for blocking dialogs. Spec §2.1 forbids accessibility services.

## When this page becomes a design doc

When Stage 9 ships, this page replaces the open questions with:

- the actual notification channel id, importance, and category;
- the FSI activity layout and behavior on lock-screen vs unlocked;
- the overlay banner Compose surface;
- the measured latency on the OEM matrix (one row per device);
- the privacy-boundary test that proves the alert surface doesn't leak campaign identifiers via `Intent` extras;
- the kill-switch behavior on permission revocation.
