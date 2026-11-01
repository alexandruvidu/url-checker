package com.example.url_checker.data

/** Final verdict shown to the user. */
enum class Verdict { SAFE, PHISHING, UNCERTAIN }

/** Where the verdict came from. */
enum class DecidedBy {
    /** urlbert-tiny on the phone was confident enough. */
    ON_DEVICE,
    /** Escalated: cloud DistilBERT + threat lists + redirects + domain age. */
    CLOUD,
    /** Needed the cloud, but no connection: request deferred, local answer shown. */
    CLOUD_PENDING_OFFLINE,
    /** Needed the cloud, but the user did not consent to sending URLs. */
    CLOUD_SKIPPED_NO_CONSENT,
}

/** Why a check was escalated (or not) to the cloud. */
enum class EscalationReason { NONE, GREY_ZONE, SHORTENER }

/** How the URL reached the app. */
enum class CheckSource { SHARE_SHEET, MANUAL, ACCESSIBILITY }

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

data class Settings(
    val cloudConsent: Boolean = true,
    /** Prototype-only switch to fake airplane mode. */
    val simulateOffline: Boolean = false,
    /** Below this the local model says SAFE. */
    val lowThreshold: Float = 0.15f,
    /** Above this the local model says PHISHING. */
    val highThreshold: Float = 0.85f,
)
