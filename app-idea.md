# App idea

Fill this in before you start prototyping. Keep it short and concrete. You will defend these answers
in the project spot-check, especially the mobile-computing framing at the bottom.

## The basics

- **App name:**
- **One-line pitch:** (what it does, for whom, in one sentence)
- **The problem:** (what real need or annoyance it addresses)
- **Target users:** (who uses it, and in what situation, for example "on the trail with no signal")

## The prototype scope

Keep this tiny: one or two screens for Lab 3.

- **Core screen(s):** (what the user sees and does first)
- **The main "thing" the app manages:** (the record or item at the centre, for example a sighting,
  a route, a plant, a run)
- **A few fields of that thing:** (name, timestamp, location, note, photo, ...)

## The data

- **What records does the app create or collect?**
- **Where does data come from?** (user input, a sensor, an API, a server)
- **Where does data need to go?** (stays local, syncs to a server, shared with others)

## Mobile-computing framing (required)

This is what makes it a mobile-computing project rather than just an app. Answer honestly; rough
answers are fine for now.

- **What must persist on the device** (survive a restart)?
- **What crosses the network** (comes from or goes to a server or API)?
- **What must still work with no signal**, and how should it behave offline?
- **The placement question:** is there any computation that could run on-device instead of in the
  cloud (for example a model, a search, image processing)? If you moved it on-device, what would you
  trade in latency, energy, privacy, and bandwidth?

## What comes next

You do not need to plan the rest of the course here. Each upcoming lab takes one part of this app
and makes it real (structure first, then networking, storage, offline behaviour, and so on). You will
sketch that roadmap for your own app in `reflection.md`, once you have built the prototype and seen
first hand where it falls short.
