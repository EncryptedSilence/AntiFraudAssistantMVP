# Web channel — **decision open**

> Stage status: **planning, no code.** This is the third observation channel after calls and SMS. Spec §4.3 leaves the mechanism explicitly open; this page is the working notebook for the choice. When the decision is made, this page becomes the design doc.

## What we want

When the user opens a domain that resembles a known bank or service for the first time, the app needs enough signal to fire a `WebEvent` and let the pattern engine match (`new_lookalike_domain_visit_v1` and successors). Minimally:

- the eTLD+1 ("registered domain") of the visited site;
- whether this device has visited that eTLD+1 before;
- a coarse timestamp.

Critically, we do **not** want full URLs, paths, query strings, cookies, logins, passwords, banking data, or anything that could function as a tracker. Spec §2.1 forbids storing those.

## Three candidate mechanisms

### Option A — `VpnService` DNS sinkhole

Stand up a local-only `VpnService` that intercepts DNS resolution. The app sees every hostname the device queries; the user's traffic is not actually proxied — DNS is observed and forwarded.

**Pros**
- No special permission beyond `BIND_VPN_SERVICE` (which the user explicitly approves once).
- No browser dependency — covers any app that resolves a hostname.
- No URL paths — DNS shows hostnames only, which already aligns with the eTLD+1-only rule.

**Cons**
- Conflicts with system VPN apps the user may already have (Android allows only one active VPN). The "always on" flag is a dealbreaker if the user runs a corporate VPN.
- Some users perceive a "VPN connection" notification as suspicious; we'd need clear in-app copy explaining why.
- We see queries from background processes (analytics, ad SDKs in other apps) that we don't care about. Filtering noise is necessary.
- DNS-over-HTTPS bypasses us silently. Coverage degrades as DoH adoption grows.

### Option B — Accessibility service address-bar reading

Use an `AccessibilityService` to read the URL bar of supported browsers when the user is in one.

**Pros**
- Sees the actual URL, not just hostname — could distinguish `bank.kz/login` from `bank.kz`. (We'd still discard everything but the eTLD+1.)
- Works regardless of DoH or VPN.

**Cons**
- Spec §2.1 hard rule: **no accessibility service.** This option is out unless the spec is changed, which would also require re-justifying the original "no a11y" decision.
- Accessibility services on Android 14+ get strict review and per-OEM nag; UX cost is high.
- Users (rightly) treat a11y permission as a major trust ask — it can read every screen.

This option is currently **rejected** by §2.1; documented here so future readers don't re-litigate.

### Option C — Accept the blind spot

Don't observe web traffic at all. The pattern engine still has the call + SMS + manual-entry signals; the lookalike-domain pattern would only fire when the user manually pastes a URL via the §17.3 manual-entry flow.

**Pros**
- Zero new permissions, zero new code, zero VPN conflicts, zero DoH bypass risk.
- Aligns with the spec's "passive observation, never invasive" framing.

**Cons**
- Drops the auto-trigger for `new_lookalike_domain_visit_v1`. The seed pattern still parses, but it never fires automatically.
- The §17.3 manual-paste fallback is more friction than the automatic path; real users won't use it.

## Decision criteria

When the choice is made, it should answer:

- Does the chosen mechanism work on the OEM matrix the project actually targets (Samsung, Xiaomi/MIUI, Huawei without GMS)?
- Does it introduce any signal that could store full URLs in the database, even temporarily? (Answer must be no.)
- Does it conflict with users running a corporate or commercial VPN?
- What's the user-visible permission ask? Is the rationale screen honest about what we observe?
- What does it do under DoH / DoT / encrypted SNI? Coverage measured, not assumed.
- What's the plan if the user revokes the permission later?

## Out of scope for this page

- The `WebEvent` schema — already defined in `:core:domain` (Stage 1). Whatever the channel choice, the captured data shape is fixed: eTLD+1 (hashed + display), `isNewDomain`, `domainStatus`, optional `webRiskScore`.
- The lookalike-domain detector. Stage 5 ships a Levenshtein check against ~20 KZ banks; it lives in `:core:scoring` or a sibling module and runs on whatever `WebEvent`s the channel produces.
- Full-URL telemetry. Forbidden regardless of the channel.

## When this page becomes a design doc

When the decision lands (likely with Stage 5), this page replaces the candidate analysis with:

- the chosen mechanism and the rationale that ruled out the alternatives;
- the specific `Permission` / `Service` declared in `AndroidManifest.xml`;
- the rationale-screen copy shown to the user;
- the OEM matrix verified;
- the privacy-boundary tests that prove no URL paths reach the database;
- the kill-switch behavior when the user revokes permission.
