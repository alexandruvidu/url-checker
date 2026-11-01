# Lab 3: The AI-Assisted Prototype (the pivot)

Everything so far has been practice on projects we handed you. This lab is the turn: **you invent your own app and build a working prototype, fast, using AI as much as you want.** It is the most open-ended and the most fun lab, and it is also a setup. By the end you will have felt, first hand, exactly what AI is brilliant at (getting a prototype on screen in an hour) and exactly where it stops (everything that makes an app production-grade). That gap is what Labs 4 to 10 are about, applied to *your* app.

This is your first project deliverable, and you build it in your own repository. If you are working in a team, everyone drives AI, everyone commits, and in the defense any of you can be asked about any part. Either way the skills are individual.

Time: about two hours. You will not finish a polished app, and you are not meant to. You are meant to finish a *prototype* and a clear-eyed list of what is missing.

---

## Part 1: Pick an app worth building (about 30 minutes)

Your app choice carries the whole rest of the course, so choose deliberately. This is a **mobile computing** course, so pick something that is a mobile-computing system, not just a screen with a form. Concretely, a good pick has all of these:

- **Data worth keeping on the device.** There are records the user creates or collects that should survive a restart.
- **Something across the network.** Data comes from, or goes to, a server or an API.
- **A reason to work offline.** The app is useful, at least partly, when there is no signal.
- **A placement question.** There is some computation that could run on the device or in the cloud, and it is not obvious which is better.

These are not hoops. They are precisely what you will build in Labs 4 to 10 (architecture, networking, persistence, offline sync, and one mobile-computing concept), so an app with all four means the rest of the course is spent making *your* idea real.

Good picks (for shape, not to copy): a field-observation logger that records sightings offline and syncs when back in coverage; a transit companion that caches routes and times and still works underground; a plant-care tracker with reminders and a species-lookup API; a run tracker that records GPS offline and uploads later. Weak picks: a tip calculator, a static info screen, anything with no data, no network, and nothing to do offline.

Keep the prototype **tiny**: one or two screens. You are proving the idea, not shipping it.

Fill in [`app-idea.md`](app-idea.md), including the mobile-computing framing at the bottom. You will defend those answers all semester.

## Part 2: Create your project (about 15 minutes)

This is the moment you own your tooling. In Android Studio: **File → New → New Project → Empty Activity** (the Compose one). Name the app, set the package, choose a minimum SDK of 26, and finish. Android Studio picks versions that match your install, so you are not tied to ours.

Then put it under version control:
```bash
git init
git add -A
git commit -m "New project from the Empty Activity template"
```
Push it to a Git host (one your team shares, if you are working in a team). From here on this is **one evolving project**, not a new folder per lab: every later lab adds to this same repo through branches and pull requests.

Copy the three templates from this folder (`app-idea.md`, `AI-USE-LOG.md`, `reflection.md`) into your new repo so your documentation lives with your code.

## Part 3: Prototype with AI (about 60 minutes)

Now go fast. Use AI freely: **Gemini in Android Studio** (built into the IDE), a chat model, or both. Reuse what you learned in Lab 2 (composables, state, lists, navigation) and let AI generate the rest.

Aim for a **clickable prototype**: your core screen or two, wired with navigation, showing data. The data can be completely fake at this stage, a hardcoded list in memory is perfect. No database and no real network yet.

### A word on "vibe coding"

The term (coined by Andrej Karpathy in 2025) means giving in to the vibes: you describe what you want in plain language, accept what the model produces, and barely look at the code. For a throwaway prototype like today's, that is allowed and genuinely fun, so lean into the speed. But notice the habit you are forming, because it is exactly the one that fails you next: from Lab 4 on you own every line in a defense, and "I accepted everything and did not read it" is not an answer. Treat today as the one time you get to vibe-code, and start practising the disciplined version below even now.

### Driving an AI assistant well (the skill you keep)

- **Give it context.** Tell it you are on Jetpack Compose with Material 3, minimum SDK 26. The more it knows about your setup, the less it guesses wrong.
- **Ask for small pieces.** One screen, one composable, or some sample data at a time, not "build my whole app." Small requests are easier to check and to fix.
- **Read and run everything.** Look at what it produced, run it, and see whether it does what you meant. If it does not compile or misses the point, say so specifically and iterate.
- **Stay in charge of the structure.** You decide the screens and the shape of the data; let AI fill in the pieces. It is a fast typist, not the architect.
- **Keep the log as you go** in [`AI-USE-LOG.md`](AI-USE-LOG.md): what you asked, what it produced, and what you kept or threw away and why. This is required, and it is far easier live than reconstructed.

To get more out of the in-IDE assistant, see [About Gemini in Android Studio](https://developer.android.com/studio/gemini/overview) and [6 expert tips for Gemini in Android Studio](https://android-developers.googleblog.com/2026/03/supercharge-your-android-development.html).

## Part 4: Find the ceiling (this is the lesson)

Once the prototype runs, deliberately push on it and watch what happens:

- **Rotate the device.** Does your state survive, or does the screen reset?
- **Kill the app and reopen it.** Is anything the user entered still there?
- **Turn on airplane mode.** Does it degrade gracefully, show nothing, or crash?
- **Add 500 items** to your list. Is it still smooth?
- **Imagine two people editing the same data.** Is there any notion of sync or conflict?

Most prototypes fall over on several of these, and that is exactly the point. AI gave you a user interface. It did not give you **architecture, persistence, offline behaviour, networking, or tests**, and it will not, because those are engineering decisions about your specific constraints, not boilerplate it can pattern-match. That is the difference between a prototype and a product, and it is the rest of this course.

In [`reflection.md`](reflection.md), write down what broke and what is missing, and map each gap to the lab that fixes it (architecture is Lab 4, networking Lab 5, persistence Lab 6, offline and sync Lab 7, dependency injection Lab 8, testing and CI Lab 9, the mobile-computing concept Lab 10).

## Part 5: The systems question

Because this is a mobile computing course, answer these for your app now (in the framing section of `app-idea.md`), and expect to revisit and defend them all semester:

- What data lives on the device, and what lives on a server?
- What must keep working with no signal?
- Is there any computation that could run on-device instead of in the cloud, and what would you trade if you moved it (latency, energy, privacy, bandwidth)?

You do not need final answers. You need to have started thinking like a mobile systems engineer about your own app.

---

## Done when

- You have your **own project repository** with a **running prototype** (your core screen or two, showing data), committed and pushed.
- `app-idea.md`, `AI-USE-LOG.md`, and `reflection.md` are filled in and live in that repo.
- If you are working in a team, **every member has made at least one commit.**

There is nothing to make "green" here: the deliverable is a prototype plus honest documentation of how you built it and what it still lacks.

## Using AI on this lab

Use AI as much as you like for the prototype itself; that is the point of the lab. But the graded substance is your **understanding**, shown two ways: the **AI-use log** (what you prompted, what you kept or rejected and why) and the **reflection** (where AI stopped and what production needs). In the defense you must be able to explain and change any line in your prototype, no matter who or what wrote it. The lesson of Lab 3 is precisely that AI gets you to a prototype and no further; the judgment in Labs 4 to 10 is the part AI cannot supply, and the part you are here to learn.

## What's next

Lab 4 takes this prototype and gives it a real structure: a ViewModel, unidirectional state, and a clean separation between UI and logic. You stop vibe-coding and start engineering, on your own app.
