package com.example.url_checker.data

/** Aggregates over the check history, shown on the statistics screen. */
data class Stats(
    val total: Int,
    val safe: Int,
    val phishing: Int,
    val uncertain: Int,
    /** How many checks ended in each [DecidedBy] state (every state present, possibly 0). */
    val decidedBy: Map<DecidedBy, Int>,
    val escalatedGreyZone: Int,
    val escalatedShortener: Int,
    val avgLocalMs: Int?,
    val avgCloudMs: Int?,
    /** Links that actually reached the server. */
    val sentToCloud: Int,
    /** Query parameters + fragments cut from all checked links. */
    val partsStripped: Int,
    /** Of those, how many looked personal (email, phone, token, ID). Tracking tags not counted. */
    val sensitiveStripped: Int,
    val bySource: Map<CheckSource, Int>,
) {
    val onDeviceShare: Float
        get() = if (total == 0) 0f else (total - sentToCloud).toFloat() / total
}

fun computeStats(history: List<UrlCheck>): Stats {
    val removed = history.flatMap { FakeUrlChecker.removedParts(it.originalUrl) }
    return Stats(
        total = history.size,
        safe = history.count { it.verdict == Verdict.SAFE },
        phishing = history.count { it.verdict == Verdict.PHISHING },
        uncertain = history.count { it.verdict == Verdict.UNCERTAIN },
        decidedBy = DecidedBy.entries.associateWith { d -> history.count { it.decidedBy == d } },
        escalatedGreyZone = history.count { it.escalation == EscalationReason.GREY_ZONE },
        escalatedShortener = history.count { it.escalation == EscalationReason.SHORTENER },
        avgLocalMs = history.map { it.localLatencyMs }.averageOrNull(),
        avgCloudMs = history.mapNotNull { it.cloudLatencyMs }.averageOrNull(),
        sentToCloud = history.count { it.decidedBy == DecidedBy.CLOUD },
        partsStripped = removed.size,
        sensitiveStripped = removed.count { it.hint != null && it.hint != "tracking tag" },
        bySource = CheckSource.entries.associateWith { s -> history.count { it.source == s } },
    )
}

private fun List<Int>.averageOrNull(): Int? = if (isEmpty()) null else average().toInt()
