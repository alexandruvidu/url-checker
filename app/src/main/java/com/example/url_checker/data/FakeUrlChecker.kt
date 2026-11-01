package com.example.url_checker.data

import java.net.URI
import java.net.URLDecoder
import kotlin.math.abs
import kotlin.random.Random

/**
 * PROTOTYPE ONLY. Stands in for urlbert-tiny (ONNX on device) and the cloud backend.
 * Scores come from simple keyword heuristics so the UI has realistic-looking data to show.
 */
object FakeUrlChecker {

    private val shorteners = setOf(
        "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly", "is.gd", "buff.ly", "cutt.ly", "rb.gy",
    )
    private val suspiciousWords = listOf(
        "login", "verify", "secure", "account", "update", "confirm", "bank", "wallet",
        "paypal", "password", "signin", "unlock", "bonus", "gift", "colet", "plata",
    )
    private val wellKnown = setOf(
        "google.com", "youtube.com", "wikipedia.org", "github.com", "emag.ro", "bt.ro",
        "anaf.ro", "upb.ro", "microsoft.com", "apple.com", "whatsapp.com",
    )

    private val urlRegex = Regex("""(https?://[^\s<>"']+|www\.[^\s<>"']+)""", RegexOption.IGNORE_CASE)

    /** Pull URLs out of a shared message. The message text itself never leaves the phone. */
    fun extractUrls(text: String): List<String> =
        urlRegex.findAll(text)
            .map { it.value.trimEnd('.', ',', ')', ';', '!', '?') }
            .map { if (it.startsWith("www.", ignoreCase = true)) "https://$it" else it }
            .distinct()
            .toList()

    /** Strip query string and fragment: they may carry user data (tokens, emails, IDs). */
    fun sanitize(url: String): String = runCatching {
        val u = URI(url)
        URI(u.scheme, u.authority, u.path, null, null).toString()
    }.getOrElse { url.substringBefore('?').substringBefore('#') }

    /** The part of [url] that is kept: everything before the query (`?`) or fragment (`#`). */
    fun keptPart(url: String): String = url.substringBefore('?').substringBefore('#')

    /** Every query parameter and the fragment that [sanitize] drops, with a hint if it looks personal. */
    fun removedParts(url: String): List<RemovedPart> {
        val query = url.substringBefore('#').substringAfter('?', "")
        val fragment = url.substringAfter('#', "")
        val params = query.split('&').filter { it.isNotEmpty() }.map { p ->
            val name = p.substringBefore('=')
            val value = p.substringAfter('=', "")
            RemovedPart(RemovedPart.Kind.QUERY_PARAM, name, value, sensitiveHint(name, value))
        }
        return if (fragment.isEmpty()) params
        else params + RemovedPart(RemovedPart.Kind.FRAGMENT, "#", fragment, sensitiveHint("", fragment))
    }

    private val emailRegex = Regex("""[^@\s]+@[^@\s]+\.[a-z]{2,}""", RegexOption.IGNORE_CASE)
    private val phoneKeys = Regex("phone|tel|mobile|msisdn", RegexOption.IGNORE_CASE)
    private val secretKeys = Regex("token|session|sid|auth|key|code|otp|pass", RegexOption.IGNORE_CASE)
    private val idKeys = Regex("^(id|uid|user|userid|user_id|client|account|cnp|awb)$", RegexOption.IGNORE_CASE)
    private val trackingKeys = Regex("^(utm_.*|fbclid|gclid|ref|mc_eid)$", RegexOption.IGNORE_CASE)

    private fun sensitiveHint(name: String, rawValue: String): String? {
        val value = runCatching { URLDecoder.decode(rawValue, "UTF-8") }.getOrDefault(rawValue)
        val digits = value.count { it.isDigit() }
        return when {
            emailRegex.containsMatchIn(value) -> "email address"
            phoneKeys.containsMatchIn(name) ||
                (digits in 9..15 && digits >= value.length - 2) -> "phone number"
            secretKeys.containsMatchIn(name) -> "login token / secret"
            idKeys.matches(name) -> "personal ID"
            trackingKeys.matches(name) -> "tracking tag"
            else -> null
        }
    }

    fun host(url: String): String =
        runCatching { URI(url).host?.removePrefix("www.") }.getOrNull() ?: url

    fun isShortener(url: String) = host(url).lowercase() in shorteners

    private fun localScore(url: String): Float {
        val h = host(url).lowercase()
        if (wellKnown.any { h == it || h.endsWith(".$it") }) return 0.02f + jitter(url) * 0.05f
        var s = 0.10f
        val lower = url.lowercase()
        s += suspiciousWords.count { it in lower } * 0.18f
        if (h.count { it == '.' } >= 3) s += 0.15f
        if (h.count { it == '-' } >= 2) s += 0.15f
        if (Regex("""\d+\.\d+\.\d+\.\d+""").matches(h)) s += 0.35f
        if (h.endsWith(".xyz") || h.endsWith(".top") || h.endsWith(".click")) s += 0.2f
        if (!lower.startsWith("https")) s += 0.1f
        return (s + jitter(url) * 0.1f).coerceIn(0.01f, 0.99f)
    }

    private fun jitter(url: String) = abs(url.hashCode() % 100) / 100f

    fun check(
        url: String,
        settings: Settings,
        source: CheckSource,
        id: Long,
        now: Long = System.currentTimeMillis(),
        /** False = stop before the cloud so the user can review what would be sent. */
        cloudApproved: Boolean = !settings.askBeforeCloud,
    ): UrlCheck {
        val rnd = Random(url.hashCode())
        val local = localScore(url)
        val sanitized = sanitize(url)
        val escalation = when {
            isShortener(url) -> EscalationReason.SHORTENER
            local > settings.lowThreshold && local < settings.highThreshold -> EscalationReason.GREY_ZONE
            else -> EscalationReason.NONE
        }
        val localVerdict = when {
            local >= settings.highThreshold -> Verdict.PHISHING
            local <= settings.lowThreshold -> Verdict.SAFE
            else -> Verdict.UNCERTAIN
        }
        val localLatency = 18 + rnd.nextInt(30)

        val base = UrlCheck(
            id = id, originalUrl = url, sanitizedUrl = sanitized,
            localScore = local, cloudScore = null,
            verdict = if (escalation == EscalationReason.SHORTENER) Verdict.UNCERTAIN else localVerdict,
            decidedBy = DecidedBy.ON_DEVICE, escalation = escalation,
            cloudSignals = emptyList(), finalUrl = null,
            localLatencyMs = localLatency, cloudLatencyMs = null,
            source = source, checkedAt = now,
        )
        if (escalation == EscalationReason.NONE) return base
        if (!settings.cloudConsent) return base.copy(decidedBy = DecidedBy.CLOUD_SKIPPED_NO_CONSENT)
        if (!cloudApproved) return base.copy(decidedBy = DecidedBy.CLOUD_AWAITING_APPROVAL)
        if (settings.simulateOffline) return base.copy(decidedBy = DecidedBy.CLOUD_PENDING_OFFLINE)

        // Fake cloud: shorteners resolve to something, grey-zone gets pushed to one side.
        val phishy = if (escalation == EscalationReason.SHORTENER) rnd.nextBoolean() else local > 0.5f
        val finalUrl = if (escalation == EscalationReason.SHORTENER) {
            if (phishy) "https://secure-login-verify.account-update.xyz/session"
            else "https://www.wikipedia.org/wiki/Phishing"
        } else sanitized
        val cloud = if (phishy) 0.9f + rnd.nextFloat() * 0.09f else 0.02f + rnd.nextFloat() * 0.08f
        val signals = listOf(
            CloudSignal("Google Safe Browsing", if (phishy) "Listed: SOCIAL_ENGINEERING" else "Not listed", phishy),
            CloudSignal("URLhaus", "Not listed", false),
            CloudSignal(
                "Redirect chain",
                if (escalation == EscalationReason.SHORTENER) "2 hops → ${host(finalUrl)}" else "No redirects",
                phishy && escalation == EscalationReason.SHORTENER,
            ),
            CloudSignal(
                "Domain age",
                if (phishy) "${2 + rnd.nextInt(10)} days" else "${3 + rnd.nextInt(20)} years",
                phishy,
            ),
        )
        return base.copy(
            cloudScore = cloud,
            verdict = if (phishy) Verdict.PHISHING else Verdict.SAFE,
            decidedBy = DecidedBy.CLOUD,
            cloudSignals = signals,
            finalUrl = finalUrl,
            cloudLatencyMs = 350 + rnd.nextInt(600),
        )
    }

    /** Hardcoded history so the list isn't empty on first launch. */
    fun sampleHistory(
        settings: Settings = Settings(cloudConsent = true, consentAsked = true, askBeforeCloud = false),
    ): List<UrlCheck> {
        val now = System.currentTimeMillis()
        val min = 60_000L
        return listOf(
            "https://www.emag.ro/telefoane-mobile/c?ref=hp_menu" to CheckSource.SHARE_SHEET,
            "http://fancourier-colet-plata.xyz/verify?id=88213&phone=0722" to CheckSource.SHARE_SHEET,
            "https://bit.ly/3xYz9Qa" to CheckSource.SHARE_SHEET,
            "https://bt-secure-login.ro-account.com/update" to CheckSource.SHARE_SHEET,
            "https://github.com/onnx/onnxruntime" to CheckSource.MANUAL,
            "http://192.168.44.12/paypal/signin" to CheckSource.MANUAL,
        ).mapIndexed { i, (url, src) ->
            check(url, settings, src, id = i.toLong() + 1, now = now - (i + 1) * 37 * min)
        }
    }
}
