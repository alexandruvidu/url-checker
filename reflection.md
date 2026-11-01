# Prototype reflection: where AI stopped

Fill this in after Part 4, once you have stress-tested your prototype. This is the bridge from Lab 3
to the rest of the course. Be honest and specific; a vague "it works fine" means you did not push
hard enough.

## What AI did well

Where did AI genuinely accelerate you? (screens, sample data, navigation, boilerplate, ...)

-
-

## What broke when you stressed it

Go through the checks from Part 4 and record what actually happened.

- **Rotate the device:** (did state survive?)
- **Kill and reopen the app:** (was anything saved?)
- **Airplane mode:** (graceful, blank, or crash?)
- **500 items in a list:** (still smooth?)
- **Two people editing the same data:** (any sync or conflict handling?)

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

## One sentence

In your own words: what is the difference between what AI gave you today and a production app, and why
can AI not simply close that gap for you?

>
