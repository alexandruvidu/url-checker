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
| 2026-11-01 | Build the prototype GUI for the phishing URL checker | "Develop the GUI for this app" + pasted design notes (share sheet, urlbert-tiny on device, cloud escalation, privacy, offline) | Migrated project from Views to Compose (Gradle, minSdk 26, removed `activity_main.xml`); `MainActivity` with NavHost + ACTION_SEND share-sheet intent filter; `HomeScreen` (paste box, stats card, history list), `ResultScreen` (verdict header, on-device/cloud score bars, cloud signals, privacy/sanitized URL, offline pending banner), `SettingsScreen` (cloud consent, thresholds, MDM accessibility placeholder, simulate offline); fake data layer `FakeUrlChecker` (URL extraction, query stripping, heuristic scores, threshold + shortener escalation), `AppState`, `Models.kt`; `UrlCheckerTheme` with fixed verdict colours | TODO: accepted/rejected + why |
| | | | | |
| | | | | |

## Prompts worth remembering

Optionally, keep a few prompts that worked especially well or especially badly, with a note on why.
This is useful for you, and for your team if you are working in one.

-
-
