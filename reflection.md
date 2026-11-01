# Prototype reflection: where AI stopped

Fill this in after Part 4, once you have stress-tested your prototype. This is the bridge from Lab 3
to the rest of the course. Be honest and specific; a vague "it works fine" means you did not push
hard enough.

## What AI did well

Where did AI genuinely accelerate you? (screens, sample data, navigation, boilerplate, ...)

- **Screens and navigation:** it built five Compose screens (consent, home, result, statistics,
  settings) wired with a NavHost, plus the share-sheet intent filter so links can be shared from
  SMS/WhatsApp. Doing this by hand would have taken most of the lab.
- **Fake data that looks real:** `FakeUrlChecker` produces believable scores, cloud signals, latencies
  and a sample history, so every screen has something to show without a model or a backend.
- **Turning the design notes into UI:** the threshold split, the reason for escalating, the offline
  "pending" state and the parameter stripping from `app-idea.md` all became visible on screen
  (score bars, "Sent to cloud because…", the struck-through "what was cut" preview).
- **Small pure functions that are easy to test:** `removedParts()` and `computeStats()` contain no UI
  code, and it wrote the first real unit test (`RemovedPartsTest`).

## What broke when you stressed it

Tested on an Android emulator (`smishield_test`, Android 14 / API 34, 1080×2340) with a debug build, on 2026-11-01.

- **Rotate the device:** state did **not** survive. On a result screen for a link I had just
  checked, rotating showed "This check is no longer in history." Going back, the history had reset
  to the 6 hardcoded samples. Worse, **settings reset silently**: after accepting cloud checks on
  the consent screen and rotating, "Cloud second opinion" was back to off, but the consent screen
  was not shown again. The screen I was on was remembered, but the data was not. The cause is
  `remember { AppState() }` in `MainActivity`: the whole Activity is recreated on rotation, and
  `remember` only lives as long as that composition.
- **Kill and reopen the app:** nothing was saved. The consent screen appeared again and the history
  was back to the 6 samples. The user's checks, the consent choice and any queued cloud checks are
  all lost.
- **Airplane mode:** no crash, and on-device checks work, but it is **not honest**. With real
  airplane mode on, I approved a cloud check for a shortened link and the app showed a "Phishing"
  verdict "decided by Cloud", with cloud signals and a cloud latency. The app never looks at the
  real network. Only the fake "Simulate offline" switch in Settings makes it defer the check. In a
  real app, that would be a fabricated result shown with full confidence.
- **500 items in a list:** I shared one message with 500 links. The app handled it, with no crash and
  all 500 checked and listed, but scrolling the home list was **not smooth**: `dumpsys gfxinfo`
  over 15 flings reported 57 of 59 frames janky (97%), median frame time 93 ms, 99th percentile
  300 ms. This is a debug build on an emulator, so a release build on a real phone would be much
  better, and I still need to measure that before blaming the code. Likely costs: every row creates
  a new `DateFormat`, and the summary card counts the whole history on every recomposition.
  It also showed a design problem: all 500 links landed in the uncertain range, so each one
  needs its own "Send link" tap. There is no "approve all", and the app only opened the first one.
- **Sharing while the app is open:** found by accident. Sharing a link while LinkGuard was already
  open started a **second copy** of the screen (two `MainActivity` instances in the
  task), each with its own separate history. One user, two inconsistent states, with no sync even
  inside one phone.

## What is missing for production

List what your prototype does not have yet. For each gap, name the lab that will address it.

| Gap in the prototype | Lab that fixes it |
|---|---|
| No separation of UI and logic; state resets | Lab 4 (architecture) |
| No data from a real server | Lab 5 (networking) |
| Nothing is saved on the device | Lab 6 (persistence) |
| Does not work offline; no background sync | Lab 7 (offline-first, WorkManager) |
| Dependencies wired by hand | Lab 8 (dependency injection) |
| No tests; nothing stops a broken change | Lab 9 (testing, CI) |
| No mobile-computing concept beyond a UI | Lab 10 (integrated concept) |

Add any gaps specific to your app that are not in the table above.

| Gap specific to LinkGuard | Lab that fixes it |
|---|---|
| `AppState` lives in `remember` inside the Activity; rotation wipes history and silently turns cloud consent off. Needs a ViewModel and a single source of truth | Lab 4 |
| Sharing while the app is open creates a second `MainActivity` with its own state (needs `singleTop`/`onNewIntent` and shared state) | Lab 4 |
| No real backend: cloud verdict, Safe Browsing/URLhaus, redirect following and domain age are all faked | Lab 5 |
| Offline is a manual switch, not the real network state (ConnectivityManager); approved checks get a fake "Cloud" verdict in airplane mode | Lab 5 / Lab 7 |
| Consent answer, "ask before each check" preference and history are not stored (DataStore for settings, Room for history) | Lab 6 |
| Deferred cloud checks are an in-memory list; they need a durable queue retried by WorkManager when the network returns | Lab 7 |
| No cache of cloud verdicts, and no expiry for stale "safe" verdicts | Lab 6 / Lab 7 |
| `FakeUrlChecker` is an `object` called directly from `AppState`; it cannot be swapped for the real ONNX model or a test double | Lab 8 |
| Only one unit test (`RemovedPartsTest`); nothing tests escalation rules, thresholds, consent gating or the "never send without approval" guarantee | Lab 9 |
| No real on-device model: urlbert-tiny via ONNX Runtime, plus thresholds picked on a validation set (PhishTank/OpenPhish vs. Tranco) | Lab 10 |
| Statistics show fake latencies; the real on-device vs. cloud latency/energy measurement is not done | Lab 10 |
| History list is janky with 500 items in a debug build; needs measuring in release and fixing (cache `DateFormat`, precompute aggregates) | Lab 4 / Lab 9 |
| One approval per uncertain link; no batch approval for a message with many links | Lab 4 (UI state) |
## One sentence

In your own words: what is the difference between what AI gave you today and a production app, and why
can AI not simply close that gap for you?

For the time being the AI could give us a working prototype from one big prompt, but we only felt in control of the code once we broke the later work down into smaller tasks and used it for implementing these ones. AI makes decent code but it's not ready for architectural decisions or what paths it should choose based on the current context. It remains a tool in our shed for the things we're going to build.
