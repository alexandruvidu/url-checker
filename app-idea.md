# App idea

Fill this in before you start prototyping. Keep it short and concrete. You will defend these answers
in the project spot-check, especially the mobile-computing framing at the bottom.

## The basics

- **App name:** LinkGuard
- **One-line pitch:** A link checker that tells you within milliseconds whether a URL you received by
  SMS, WhatsApp or email is phishing. It uses an ML model that runs locally on the phone and calls
  the cloud only when the local model is unsure.
- **The problem:** Phishing links reach the phone through every messaging channel, and the user has
  to decide on the spot whether to tap. On a small phone screen you can barely see the full URL,
  shortened links hide where they really go, and opening the page to "see what it is" is exactly the
  risk we want to avoid. The check looks **only at the URL** and never opens the page on the phone.
- **Target users:** everyday users who get a suspicious message and want a quick answer. They share
  the message from any app (SMS, WhatsApp, email) to LinkGuard, or paste the link by hand.

## The prototype scope

Keep this tiny: one or two screens for Lab 3.

- **Core screen(s):**
  1. **Check:** takes the URL (from the share sheet or pasted by hand) and shows the verdict (safe /
     suspicious / phishing), the probability the model gave, and whether the answer came from the
     device or the cloud.
  2. **History:** the list of checked links, each with its verdict and time.
- **The main "thing" the app manages:** a **link check** (a URL together with its verdict).
- **A few fields of that thing:**
  - the URL (without query parameters) and its domain
  - when it was checked (timestamp)
  - the local model's score (phishing probability)
  - the final verdict and where it came from (local / cloud)
  - the cloud check status (not needed / pending / done / failed)
  - the reasons for the verdict (for example: shortened link, listed in Safe Browsing/URLhaus,
    recently registered domain, redirects)
  - how long the check took (latency)

For the Lab 3 prototype, the data and verdicts are fake (an in-memory list). The model and the
backend come in later labs.

## The data

- **What records does the app create or collect?** One record per checked link (the fields above),
  plus the user's settings (mainly consent to send URLs to the cloud) and the queue of cloud requests
  that were postponed because there was no connection.
- **Where does data come from?**
  - The URL comes from the share sheet (text shared from another app, which we extract the link
    from) or is pasted by hand.
  - The local score comes from the **urlbert-tiny** model (a very small BERT trained on phishing
    URLs, in ONNX format), run with ONNX Runtime for Android.
  - The cloud verdict comes from our backend.
- **Where does data need to go?** The history stays on the device. Only the URL (with its
  parameters stripped) goes to the backend, and only when escalation is needed and the user has given
  consent. The message text never leaves the phone.

## Mobile-computing framing (required)

This is what makes it a mobile-computing project rather than just an app. Answer honestly; rough
answers are fine for now.

- **What must persist on the device** (survive a restart)?
  - the ONNX model and the decision thresholds;
  - the check history;
  - the cloud consent setting and whether the first-run consent screen was answered. This one is
    privacy-critical: if it is
    lost, the app must fall back to "no consent" and ask again, never assume a yes;
  - the "ask before each cloud check" preference, and the checks still waiting for the user's
    approval;
  - the queue of postponed cloud requests, so they are not lost on a restart;
  - possibly a cache of cloud verdicts, so a link that was already checked is not sent again.
- **What crosses the network** (comes from or goes to a server or API)?
  - **To the cloud:** only the URL, without parameters (parameters can contain user data), and only
    with the user's consent. Consent is opt-in and asked on first launch. By default the user also
    approves each send: before anything leaves the phone, the app shows the link with the query and
    fragment struck through, lists every removed parameter (flagging ones that look like an email,
    phone number, login token or ID), and waits for "Send" or "Keep on device". We also considered sending hashes, but the backend needs the actual URL
    to follow redirects and check the domain, so a hash would not be enough.
  - **From the cloud:** the verdict and its reasons. The backend runs a larger model (a DistilBERT
    for URLs) and does the checks the phone cannot do: up-to-date threat lists (Google Safe
    Browsing, URLhaus), following redirects, and domain age.
- **What must still work with no signal**, and how should it behave offline?
  - The local check works fully offline: the user gets the on-device model's verdict right away.
  - If the link also needed a cloud check (score in the uncertain zone, or a shortened link), the
    request is postponed and queued, and the user sees a message that the full check could not be
    done right now. This is shown together with the local verdict, which is marked as provisional.
  - The history, the statistics and the "what was cut" preview are all computed on the device, so
    they work offline too.
  - "Offline" has to come from the real network state (ConnectivityManager), not from an in-app
    switch. Testing the prototype in real airplane mode showed it still returning a "cloud" verdict,
    because only the fake "Simulate offline" switch was checked.
- **The placement question:** is there any computation that could run on-device instead of in the
  cloud (for example a model, a search, image processing)? If you moved it on-device, what would you
  trade in latency, energy, privacy, and bandwidth?

  Classification is exactly this question, and our answer is a split based on thresholds:
  - The local model gives a phishing probability. If the result is clearly safe or clearly phishing,
    the answer stays local. If it falls somewhere in the middle, the URL goes to the cloud.
    **Shortened links always go to the cloud**, because you cannot tell from them where they really
    lead.
  - We pick the thresholds on a validation set so that the local model is wrong less than a set
    percentage of the time (for example 1%) in the zones where it decides on its own. We evaluate on
    recent URLs from PhishTank/OpenPhish and on legitimate domains from the Tranco list.

  The trade-offs:
  - **Latency:** locally the answer is almost instant and does not depend on the network; the cloud
    adds the network round trip plus following redirects and querying external lists.
  - **Energy:** a very small model uses little power for inference; a network request wakes up the
    radio, which on mobile is often more expensive than the inference itself.
  - **Privacy:** locally nothing leaves the phone; with the cloud the URL leaves (minimized by
    stripping parameters, only with consent, and by default only after the user has seen exactly
    what will be sent). The trade-off here is friction: every grey-zone link costs the user a tap,
    so a large batch of uncertain links means many approvals.
  - **Bandwidth:** locally the only cost is downloading the model once (the app gets bigger); with
    the cloud every check is a request, but a small one.
  - **What the cloud gains:** a larger model and fresh data (threat lists, redirects, domain age)
    that the phone cannot have.

  What we will measure: how often checks go to the cloud, how much accuracy improves compared to the
  local-only version, and the latency of each. The prototype already has a statistics screen with
  these numbers (share decided on device vs. cloud, why links were escalated, average latency of
  each path, parameters stripped). For now they come from fake data, but the same screen will show
  the real measurements later.

## What comes next

You do not need to plan the rest of the course here. Each upcoming lab takes one part of this app
and makes it real (structure first, then networking, storage, offline behaviour, and so on). You will
sketch that roadmap for your own app in `reflection.md`, once you have built the prototype and seen
first hand where it falls short.
