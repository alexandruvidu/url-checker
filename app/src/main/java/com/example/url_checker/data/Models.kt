package com.example.url_checker.data

/** Final verdict shown to the user. */
enum class Verdict { SAFE, PHISHING, UNCERTAIN }

/** Where the verdict came from. */
enum class DecidedBy {
    /** urlbert-tiny on the phone was confident enough. */
    ON_DEVICE,
    /** Escalated: cloud DistilBERT + threat lists + redirects + domain age. */
    CLOUD,
    /** Needs the cloud; waiting for the user to approve exactly what will be sent. */
    CLOUD_AWAITING_APPROVAL,
    /** Needed the cloud, but no connection: request deferred, local answer shown. */
    CLOUD_PENDING_OFFLINE,
    /** Needed the cloud, but the user did not consent to sending URLs. */
    CLOUD_SKIPPED_NO_CONSENT,
    /** The user reviewed the stripped link and chose not to send it. */
    CLOUD_DECLINED,
}

/** Why a check was escalated (or not) to the cloud. */
enum class EscalationReason { NONE, GREY_ZONE, SHORTENER }

/** How the URL reached the app. */
enum class CheckSource { SHARE_SHEET, MANUAL }

/** One extra check that only the backend can do. */
data class CloudSignal(
    val name: String,       // e.g. "Google Safe Browsing"
    val result: String,     // e.g. "Not listed"
    val bad: Boolean,
)

data class UrlCheck(
    val id: Long,
    val originalUrl: String,
    /** The URL with query + fragment stripped: the only thing that ever leaves the phone. */
    val sanitizedUrl: String,
    val localScore: Float,          // phishing probability from the on-device model
    val cloudScore: Float?,         // phishing probability from the cloud model, if consulted
    val verdict: Verdict,
    val decidedBy: DecidedBy,
    val escalation: EscalationReason,
    val cloudSignals: List<CloudSignal>,
    val finalUrl: String?,          // where redirects ended up (cloud only)
    val localLatencyMs: Int,
    val cloudLatencyMs: Int?,
    val source: CheckSource,
    val checkedAt: Long,
)

/** One piece cut from a URL before it may leave the phone. */
data class RemovedPart(
    val kind: Kind,
    val name: String,       // parameter name, or "#" for the fragment
    val value: String,
    /** Why this piece looks sensitive, e.g. "phone number"; null if nothing recognised. */
    val hint: String?,
) {
    enum class Kind { QUERY_PARAM, FRAGMENT }
}

data class Settings(
    /** Opt-in: off until the user agrees on the consent screen. */
    val cloudConsent: Boolean = false,
    /** Whether the first-run consent screen has been answered. */
    val consentAsked: Boolean = false,
    /** Show the stripped link and wait for approval before each cloud check. */
    val askBeforeCloud: Boolean = true,
    /** Prototype-only switch to fake airplane mode. */
    val simulateOffline: Boolean = false,
    /** Below this the local model says SAFE. */
    val lowThreshold: Float = 0.15f,
    /** Above this the local model says PHISHING. */
    val highThreshold: Float = 0.85f,
)
