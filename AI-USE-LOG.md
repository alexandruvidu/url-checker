# AI-use log

You are encouraged to use AI throughout this project. In return you keep this log. It is part of
every milestone, not just Lab 3, so keep adding to it all semester. It is quick if you do it as you
work and painful if you reconstruct it later.

The point is not to prove you used AI, or that you did not. The point is to show that you **own** what
you submitted: that you understood what the model produced, judged it, and decided what to keep. In
the defense you must be able to explain and modify any of it.

## How to log

One row per meaningful AI interaction. You do not need to log trivial autocompletions; do log
anything where the model produced a chunk of code or a design decision you accepted or rejected.

| Date | What you were trying to do | What you prompted (short) | What the model produced | What you accepted or rejected, and WHY |
|---|---|---|---|---|
| 2026-11-01 | Build the prototype GUI for the phishing URL checker | "Develop the GUI for this app" + pasted design notes (share sheet, urlbert-tiny on device, cloud escalation, privacy, offline) | Migrated project from Views to Compose (Gradle, minSdk 26, removed `activity_main.xml`); `MainActivity` with NavHost + ACTION_SEND share-sheet intent filter; `HomeScreen` (paste box, stats card, history list), `ResultScreen` (verdict header, on-device/cloud score bars, cloud signals, privacy/sanitized URL, offline pending banner), `SettingsScreen` (cloud consent, thresholds, simulate offline); fake data layer `FakeUrlChecker` (URL extraction, query stripping, heuristic scores, threshold + shortener escalation), `AppState`, `Models.kt`; `UrlCheckerTheme` with fixed verdict colours | TODO: accepted/rejected + why |
| 2026-11-01 | Add first-run consent, a "what gets cut" review before cloud escalation, and a statistics screen | "add consent window at the start, show what was cut from a link before escalating to cloud, a statistics screen" | `ConsentScreen` (first-run, opt-in: cloud consent now defaults to off; share-sheet links wait until answered); new `DecidedBy.CLOUD_AWAITING_APPROVAL` / `CLOUD_DECLINED` + `Settings.askBeforeCloud`; `FakeUrlChecker.removedParts/keptPart` flag params as email/phone/token/ID/tracking; `SanitizedUrlDiff` (struck-through cut parts) used in `ResultScreen` `ApprovalCard` + privacy section; `AppState.approveCloud/declineCloud/completeConsent`; `computeStats` (`Stats.kt`) + `StatsScreen`; Home stats entry points; `RemovedPartsTest` | **Accepted.** Opt-in consent (off by default) matches the idea doc's "only with the user's consent" and is the GDPR-safe default, even though fewer links get a cloud check. Kept "ask before each cloud check" on by default because seeing exactly what is sent is the app's core privacy promise; the stress test showed its cost (500 links = 500 taps), noted as a gap rather than removed. The personal-data labels (email/phone/token/ID) are simple keyword/pattern rules, so I treat them as a hint for the user, not a guarantee; the stripping itself does not depend on them. Checked it compiles and the unit test passes. |
| 2026-11-01 | Stress test the app in different scenarios (rotation, kill/reopen, airplane mode, 500 items, concurrent edits) and document what breaks | "stress test the app in different scenarios" | Ran the Part 4 stress tests on the emulator via adb (rotation, force-stop, real airplane mode, 500 links via one share intent + `dumpsys gfxinfo`) and wrote the results into `reflection.md` with an app-specific gap→lab table; updated the mobile-computing framing in `app-idea.md` (consent + per-send approval, real connectivity vs. "Simulate offline", stats screen as the measurement tool). "One sentence" left for me | **Accepted**, after checking the screenshots for the key findings (rotation reset + consent silently off, the fake "Cloud" verdict in real airplane mode, the second copy of the screen on share). The airplane-mode result is the most important: it proves "offline" must come from the real network state. Kept the 500-item jank numbers but with the caveat that they come from a debug build on an emulator; they need re-measuring in release before I blame the code. Kept the gap→lab table as a roadmap. |
| | | | | |
| | | | | |

## Prompts worth remembering

Optionally, keep a few prompts that worked especially well or especially badly, with a note on why.
This is useful for you, and for your team if you are working in one.

-
-
