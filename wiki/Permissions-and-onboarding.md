# Permissions and onboarding — **stub, fills in across Stages 3, 4, 9**

> Stage status: **planning.** Spec Appendix C is the authoritative permission matrix. This page is the UX-side companion: what gets requested, when, what the rationale screen says, and what happens when the user denies.

## Stage 1 + Stage 2 baseline

The current Stage 1 / Stage 2 build runs entirely without runtime permissions:

- The Compose status screen and demo importer only read in-memory fixtures.
- Room + SQLCipher use `INTERNAL_STORAGE` only — no manifest permission needed.
- No call observer yet, no SMS receiver yet, no foreground service yet, no notification channel yet, no overlay.

So onboarding *today* is "install the APK, open it, run a demo". The interesting permission UX starts with Stage 3.

## Permission matrix (planned, per Appendix C)

| Permission | Stage | Purpose | Granular consent? | Denial degradation |
|---|---|---|---|---|
| `READ_PHONE_STATE` | 3 | observe `TelephonyCallback` for active call state | yes — single rationale | manual-entry only for calls |
| `READ_CALL_LOG` | 3 | reconcile missed-call events on idle | yes — separate rationale | partial: live calls work, idle reconciliation does not |
| `RECEIVE_SMS` | 4 | receive `SMS_RECEIVED_ACTION` broadcasts | yes — single rationale | manual-entry only for SMS |
| `READ_SMS` | 4 | content-provider sweep to recover broadcasts missed during app standby | yes — separate rationale | partial: live SMS works, recovery does not |
| `POST_NOTIFICATIONS` (Android 13+) | 9 | the alert surface itself | yes | overlay-only at critical level if granted; otherwise no surface |
| `USE_FULL_SCREEN_INTENT` (Android 14+) | 9 | full-screen-intent activity | install-time, but Android 14+ may require user toggle in Settings | overlay-only at critical level |
| `SYSTEM_ALERT_WINDOW` | 9 | overlay banner at critical | yes — special permission with system Settings flow | full-screen intent only |
| `FOREGROUND_SERVICE_PHONE_CALL` | 3 | call-observer service type | install-time | n/a |
| `FOREGROUND_SERVICE_DATA_SYNC` | 6 / 9 | sync + alert service type | install-time | n/a |
| `INTERNET` | 6 | sync client | install-time | sync disabled |

Explicitly **not** requested: `RECORD_AUDIO`, `CAMERA`, accessibility role, default-Phone role, default-SMS role, default-Call-Screening role, `WRITE_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE`. See [Privacy boundaries](Privacy-boundaries.md).

## Onboarding UX (open)

The first-launch flow needs to:

1. Explain what the app does in plain language. No marketing.
2. Walk through each runtime permission as it's relevant — never bulk-request.
3. For each permission: show what unlocks if granted, what degrades gracefully if denied. The user must be able to deny without breaking the app.
4. Show an honest estimate of "what this app is observing right now" (the always-running ongoing notification mirrors this — see [Real-time alert surface](Real-time-alert-surface.md)).
5. Let the user revisit any permission decision later from Settings.

Open questions for Stage 8 (Settings) and Stage 9 (alert surface):

- **Order of asks.** Should permissions be requested sequentially during onboarding, or lazily as features are first used? Lazy is friendlier but bumps the first-real-use experience with a system dialog.
- **Rationale-screen copy.** §24 #11 — Russian primary, KZ + EN added post-MVP. The first-pass copy will be a placeholder until UX review.
- **Permission revocation handling.** When the user revokes a permission later, the app needs to detect this on next foreground entry and show a degraded-state banner rather than silently failing.
- **Battery-saver / Doze interaction.** Some OEM battery savers stop foreground services aggressively. Does onboarding nudge the user to whitelist the app, and if so, how does the copy explain the trade-off honestly?
- **First-run demo.** Stage 1 already ships a demo-data button that produces synthetic events without any permissions. Should onboarding offer to run that demo immediately so the user sees value before being asked for anything?

## Out of scope for this page

- The actual alert surface — that lives in [Real-time alert surface](Real-time-alert-surface.md).
- The §17 home-screen layout. Pinned passive-first per §17.1; the permission state surfaces there as a status row.
- The settings UX for sensitivity, retention, and pattern enable/disable. Stage 8.

## When this page becomes a design doc

When Stages 3, 4, 8, and 9 each ship, fill in:

- the actual rationale-screen copy for each permission (after UX review);
- screenshots of the onboarding flow per OEM;
- the test plan for granted-vs-denied behavior on each permission;
- the privacy-boundary test that confirms denied-permission code paths cannot exfiltrate via a fallback (e.g. a denied SMS read does not silently fall back to a clipboard scrape).
