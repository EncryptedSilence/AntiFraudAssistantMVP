# Stage 8 wiki updates

The wiki repository at `AntiFraud-wiki` should be refreshed when convenient to reflect Stage 8's landed work. Items below are change-list pointers; the actual wiki edits land separately.

## Pages to update

- **Home** — status block: "Stages 1–7 + Stage 9 complete" → "Stages 1–9 complete". Add a Stage 8 summary paragraph mirroring the README's Stage-8 paragraph.
- **Modules** — add a row for `:feature:settings`. Describe its responsibility (`UserSettings` persistence, `OnboardingSequencer`, `QuestionFatigueGate`, `EducationalCardScheduler`, `RetentionDisplay`) and its public types.
- **Permissions and onboarding** — replace the §22 Stage 8 description with the actual ordered list now wired in `OnboardingSequencer`. Pin the SDK gating: NOTIFICATIONS only on API 33+; FULL_SCREEN_INTENT only on API 34+. Note the Stage 9 wiring contract — FULL_SCREEN_INTENT and OVERLAY_WINDOW grants/denials are logged via `AlertPermissionResultLogger.log(...)` with the canonical permission name.
- **Architecture / Data flow** — add the new top-level nav structure (Home / Campaigns / Patterns / References / Privacy + Onboarding + Settings + CampaignDetail). Explain how `UserSettings` is the single source of truth for the §18 toggles, and how the `QuestionFatigueGate` gates question prompts on every navigation into Campaign detail.
- **Privacy boundaries** — add the source-scan from T55: only three production manifests declare permissions (`:feature:calls`, `:feature:sms`, `:core:sync`). `:feature:settings` and `:feature:alerts` declare none.
- **Roadmap** — strike "Stage 8: UX & acceptance"; the remaining items are post-MVP (user-pattern wizard UI, Russian / Kazakh translations, full §8.3 detail levels, `ActivityResultLauncher` host plumbing for the onboarding sequencer).

## Glossary additions

- **`UserSettings`** — `SharedPreferences`-backed persistence for §18 module toggles, sensitivity, onboarding-completed flag, advanced-rules flag, educational-cards bookkeeping. File name `antifraud_user_prefs`.
- **`OnboardingSequencer`** — pure-logic state machine producing the ordered §22 Stage 8 permission step list, with SDK-aware filtering for `POST_NOTIFICATIONS` (API 33+) and `USE_FULL_SCREEN_INTENT` (API 34+).
- **`QuestionFatigueGate`** — pure-logic gate that returns the next `QuestionPromptKind` to ask for a campaign, enforcing the §5.5.1 / §5.5.3 rules (HIGH+ only, ≤3 per campaign per 24 h, no re-ask, don't-ask-again).
- **`PauseBeforeActionModal`** — non-cancelable `AlertDialog` shown on Home when the active-campaign band is `RiskBand.CRITICAL`.
- **`EducationalCardScheduler`** — pure-logic gate enforcing the §19A 24 h cap on educational-card display, gated by `UserSettings.educationalCardsEnabled`.

## What stays the same

- The privacy model (§2.1 hard rules) is unchanged. Stage 8 adds no new permissions; the source-scan in T55 confirms the bound.
- The data model is unchanged. Stage 8 reads existing `RiskCampaign`, `UserAnswer`, `PatternStateEntity`, `ContactProfile`, `ExportProfile` rows.
- Sync (§7) is unchanged. Stage 8 displays the bundle manifest on References.
