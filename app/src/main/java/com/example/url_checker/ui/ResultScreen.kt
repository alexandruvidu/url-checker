package com.example.url_checker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.url_checker.data.CheckSource
import com.example.url_checker.data.CloudSignal
import com.example.url_checker.data.DecidedBy
import com.example.url_checker.data.EscalationReason
import com.example.url_checker.data.FakeUrlChecker
import com.example.url_checker.data.Settings
import com.example.url_checker.data.UrlCheck
import com.example.url_checker.ui.components.LabeledValue
import com.example.url_checker.ui.components.MonoText
import com.example.url_checker.ui.components.SanitizedUrlDiff
import com.example.url_checker.ui.components.SectionCard as Section
import com.example.url_checker.ui.components.decidedByLabel
import com.example.url_checker.ui.components.formatTime
import com.example.url_checker.ui.components.verdictStyle
import com.example.url_checker.ui.theme.LocalVerdictColors
import com.example.url_checker.ui.theme.UrlCheckerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    check: UrlCheck?,
    settings: Settings,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onApproveCloud: () -> Unit,
    onDeclineCloud: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Result") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
            )
        },
    ) { padding ->
        if (check == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("This check is no longer in history.")
            }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            VerdictHeader(check)
            if (check.decidedBy == DecidedBy.CLOUD_AWAITING_APPROVAL) {
                ApprovalCard(check, "Send for a second opinion?", onApproveCloud, onDeclineCloud)
            }
            if (check.decidedBy == DecidedBy.CLOUD_DECLINED) {
                ApprovalCard(check, "You kept this link on your phone", onApproveCloud, onDecline = null)
            }
            if (check.decidedBy == DecidedBy.CLOUD_PENDING_OFFLINE) {
                PendingBanner(offline = settings.simulateOffline, onRetry = onRetry)
            }
            if (check.decidedBy == DecidedBy.CLOUD_SKIPPED_NO_CONSENT) {
                InfoCard(
                    "Cloud check is turned off",
                    "The on-device model wasn't sure about this link. Enable cloud checks in Settings to get " +
                        "a second opinion (only the link, without its parameters, is sent).",
                )
            }
            ModelSection(check, settings)
            if (check.decidedBy == DecidedBy.CLOUD) CloudSection(check)
            PrivacySection(check)
        }
    }
}

@Composable
private fun VerdictHeader(check: UrlCheck) {
    val s = verdictStyle(check)
    Card(colors = CardDefaults.cardColors(containerColor = s.container, contentColor = s.content)) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(s.icon, null, Modifier.size(36.dp))
                Spacer(Modifier.width(12.dp))
                Text(s.label, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                when {
                    check.decidedBy == DecidedBy.CLOUD_PENDING_OFFLINE ->
                        "We couldn't finish checking this link because you're offline. Don't open it yet."
                    check.decidedBy == DecidedBy.CLOUD_AWAITING_APPROVAL ->
                        "The on-device check isn't enough for this link. Review what would be sent below."
                    s.label == "Phishing" -> "Don't open this link and don't enter any passwords or card details."
                    s.label == "Looks safe" -> "No signs of phishing were found in this link."
                    else -> "We're not sure about this link. Be careful before opening it."
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(12.dp))
            MonoText(check.originalUrl)
            Spacer(Modifier.height(4.dp))
            Text(
                "${sourceLabel(check.source)} · ${formatTime(check.checkedAt)}",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

/** Shown before escalating: exactly what was cut from the link and what the server would receive. */
@Composable
private fun ApprovalCard(check: UrlCheck, title: String, onApprove: () -> Unit, onDecline: (() -> Unit)?) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                when (check.escalation) {
                    EscalationReason.SHORTENER ->
                        "This is a shortened link. Your phone can't see where it leads without opening it; " +
                            "the server can follow the redirects safely."
                    else ->
                        "The on-device model rates this ${pct(check.localScore)} likely phishing, which is " +
                            "too close to call. The server can check threat lists and the domain's age."
                },
                style = MaterialTheme.typography.bodySmall,
            )
            HorizontalDivider()
            SanitizedUrlDiff(check.originalUrl, check.sanitizedUrl, sentLabel = "Would send")
            Text(
                "The message text is never sent.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                onDecline?.let { OutlinedButton(onClick = it) { Text("Keep on device") } }
                Button(onClick = onApprove) { Text(if (onDecline == null) "Send anyway" else "Send link") }
            }
        }
    }
}

@Composable
private fun PendingBanner(offline: Boolean, onRetry: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Cloud check deferred",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                "This link needs a cloud check (it's a shortened link or the on-device model wasn't confident). " +
                    "The request is queued and will run when you're back online.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            if (!offline) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onRetry) { Text("Retry now") }
            }
        }
    }
}

@Composable
private fun ModelSection(check: UrlCheck, settings: Settings) {
    Section("How we decided") {
        LabeledValue("Decided by", decidedByLabel(check.decidedBy))
        LabeledValue(
            "Sent to cloud because",
            when (check.escalation) {
                EscalationReason.NONE -> "Not sent"
                EscalationReason.GREY_ZONE -> "Model unsure"
                EscalationReason.SHORTENER -> "Shortened link"
            },
        )
        Spacer(Modifier.height(4.dp))
        ScoreBar("On-device model (urlbert-tiny)", check.localScore, settings)
        Text(
            "Safe ≤ ${pct(settings.lowThreshold)} · unsure in between · phishing ≥ ${pct(settings.highThreshold)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        check.cloudScore?.let { ScoreBar("Cloud model (DistilBERT)", it, settings) }
        HorizontalDivider()
        LabeledValue("On-device latency", "${check.localLatencyMs} ms")
        check.cloudLatencyMs?.let { LabeledValue("Cloud latency", "$it ms") }
    }
}

@Composable
private fun ScoreBar(label: String, score: Float, settings: Settings) {
    val vc = LocalVerdictColors.current
    val color = when {
        score >= settings.highThreshold -> vc.onPhishing
        score <= settings.lowThreshold -> vc.onSafe
        else -> vc.onUncertain
    }
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${pct(score)} phishing", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            strokeCap = StrokeCap.Round,
            drawStopIndicator = {},
        )
    }
}

@Composable
private fun CloudSection(check: UrlCheck) {
    Section("Cloud checks") {
        check.cloudSignals.forEach { SignalRow(it) }
        check.finalUrl?.let {
            HorizontalDivider()
            Text("Final destination", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            MonoText(it)
        }
    }
}

@Composable
private fun SignalRow(signal: CloudSignal) {
    val vc = LocalVerdictColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (signal.bad) Icons.Filled.Warning else Icons.Filled.CheckCircle,
            null,
            tint = if (signal.bad) vc.onPhishing else vc.onSafe,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(signal.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(signal.result, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PrivacySection(check: UrlCheck) {
    Section("Privacy") {
        val sent = check.decidedBy == DecidedBy.CLOUD
        Text(
            if (sent) "Only this was sent to the server (parameters removed):"
            else "Nothing left your phone. If a cloud check is needed, only this would be sent:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SanitizedUrlDiff(check.originalUrl, check.sanitizedUrl, sentLabel = if (sent) "Sent" else "Would send")
        Text(
            "The message text is never sent.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(body, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun pct(f: Float) = "${(f * 100).toInt()}%"

private fun sourceLabel(s: CheckSource) = when (s) {
    CheckSource.SHARE_SHEET -> "Shared from another app"
    CheckSource.MANUAL -> "Pasted manually"}

@Preview(showBackground = true)
@Composable
private fun ResultPreview() {
    val s = Settings(cloudConsent = true) // askBeforeCloud defaults to true → preview shows the approval card
    UrlCheckerTheme(dynamicColor = false) {
        ResultScreen(
            FakeUrlChecker.check("https://bit.ly/3xYz9Qa?utm=sms", s, CheckSource.SHARE_SHEET, 1),
            s, {}, {}, {}, {},
        )
    }
}
