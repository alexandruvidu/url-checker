package com.example.url_checker.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * PROTOTYPE ONLY: plain in-memory state, created in MainActivity.
 * Lost on process death (and on rotation, since it's only `remember`ed) — Lab 4/6 material.
 */
class AppState {
    var settings by mutableStateOf(Settings())
    val history = mutableStateListOf<UrlCheck>().apply { addAll(FakeUrlChecker.sampleHistory()) }
    private var nextId = 1000L

    /** Checks every URL found in [text]; returns the new checks (newest first). */
    fun checkText(text: String, source: CheckSource): List<UrlCheck> {
        val results = FakeUrlChecker.extractUrls(text)
            .map { FakeUrlChecker.check(it, settings, source, nextId++) }
        history.addAll(0, results)
        return results
    }

    fun find(id: Long): UrlCheck? = history.firstOrNull { it.id == id }

    /** Re-run deferred checks once "back online". */
    fun retryPending() {
        history.replaceAll { c ->
            if (c.decidedBy == DecidedBy.CLOUD_PENDING_OFFLINE)
                FakeUrlChecker.check(c.originalUrl, settings, c.source, c.id, c.checkedAt, cloudApproved = true)
            else c
        }
    }

    /** Answer from the first-run consent screen. */
    fun completeConsent(allowCloud: Boolean) {
        settings = settings.copy(cloudConsent = allowCloud, consentAsked = true)
    }

    /** User reviewed the stripped link and agreed to send it. */
    fun approveCloud(id: Long) = replace(id) {
        FakeUrlChecker.check(it.originalUrl, settings, it.source, it.id, it.checkedAt, cloudApproved = true)
    }

    /** User reviewed the stripped link and kept it on the phone: the local verdict stands. */
    fun declineCloud(id: Long) = replace(id) { it.copy(decidedBy = DecidedBy.CLOUD_DECLINED) }

    private fun replace(id: Long, f: (UrlCheck) -> UrlCheck) {
        val i = history.indexOfFirst { it.id == id }
        if (i >= 0) history[i] = f(history[i])
    }

    fun clearHistory() = history.clear()
}
