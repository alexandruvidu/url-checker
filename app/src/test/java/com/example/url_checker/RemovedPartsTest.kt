package com.example.url_checker

import com.example.url_checker.data.FakeUrlChecker
import com.example.url_checker.data.RemovedPart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemovedPartsTest {

    @Test
    fun queryAndFragmentAreCutAndFlagged() {
        val url = "https://x.xyz/track?awb=88213&phone=0722123456&e=a%40b.ro&utm_source=sms#pay"
        val parts = FakeUrlChecker.removedParts(url)

        assertEquals(listOf("awb", "phone", "e", "utm_source", "#"), parts.map { it.name })
        assertEquals(
            listOf("personal ID", "phone number", "email address", "tracking tag", null),
            parts.map { it.hint },
        )
        assertEquals(RemovedPart.Kind.FRAGMENT, parts.last().kind)
        assertEquals("https://x.xyz/track", FakeUrlChecker.keptPart(url))
        assertEquals(FakeUrlChecker.keptPart(url), FakeUrlChecker.sanitize(url))
    }

    @Test
    fun cleanLinkHasNothingToCut() {
        assertTrue(FakeUrlChecker.removedParts("https://bit.ly/3xYz9Qa").isEmpty())
    }
}
