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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.url_checker.data.AppState
import com.example.url_checker.data.CheckSource
import com.example.url_checker.data.DecidedBy
import com.example.url_checker.data.Stats
import com.example.url_checker.data.computeStats
import com.example.url_checker.ui.components.LabeledValue
import com.example.url_checker.ui.components.SectionCard
import com.example.url_checker.ui.components.decidedByLabel
import com.example.url_checker.ui.theme.LocalVerdictColors
import com.example.url_checker.ui.theme.UrlCheckerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(state: AppState, onBack: () -> Unit) {
    val stats = computeStats(state.history)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
            )
        },
    ) { padding ->
        if (stats.total == 0) {
            Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "No checks yet. Statistics appear once you've checked a few links.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
            VerdictTiles(stats)
            DecidedSection(stats)
            EscalationSection(stats)
            SpeedSection(stats)
            PrivacySection(stats)
            SourceSection(stats)
        }
    }
}

@Composable
private fun VerdictTiles(stats: Stats) {
    val vc = LocalVerdictColors.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Tile("${stats.total}", "checked", MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer, Modifier.weight(1f))
        Tile("${stats.safe}", "safe", vc.safe, vc.onSafe, Modifier.weight(1f))
        Tile("${stats.phishing}", "phishing", vc.phishing, vc.onPhishing, Modifier.weight(1f))
        Tile("${stats.uncertain}", "unsure", vc.uncertain, vc.onUncertain, Modifier.weight(1f))
    }
}

@Composable
private fun Tile(value: String, label: String, container: Color, content: Color, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = container, contentColor = content)) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun DecidedSection(stats: Stats) {
    SectionCard("Where checks were decided") {
        Text(
            "${pct(stats.onDeviceShare)} of links never left your phone.",
            style = MaterialTheme.typography.bodyMedium,
        )
        stats.decidedBy.filterValues { it > 0 }.forEach { (d, n) ->
            ShareBar(decidedByLabel(d), n, stats.total)
        }
    }
}

@Composable
private fun EscalationSection(stats: Stats) {
    val escalated = stats.escalatedGreyZone + stats.escalatedShortener
    SectionCard("Needed a second opinion") {
        Text(
            "$escalated of ${stats.total} links were too close to call on the device or hidden behind a shortener.",
            style = MaterialTheme.typography.bodyMedium,
        )
        ShareBar("Model unsure", stats.escalatedGreyZone, stats.total)
        ShareBar("Shortened link", stats.escalatedShortener, stats.total)
        val skipped = stats.decidedBy.getValue(DecidedBy.CLOUD_SKIPPED_NO_CONSENT) +
            stats.decidedBy.getValue(DecidedBy.CLOUD_DECLINED)
        if (skipped > 0) {
            Text(
                "$skipped stayed on device because cloud checks were off or you declined.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SpeedSection(stats: Stats) {
    SectionCard("Speed") {
        LabeledValue("Average on-device check", stats.avgLocalMs?.let { "$it ms" } ?: "–")
        LabeledValue("Average cloud check", stats.avgCloudMs?.let { "$it ms" } ?: "–")
        if (stats.avgLocalMs != null && stats.avgCloudMs != null && stats.avgLocalMs > 0) {
            Text(
                "A cloud check takes about ${stats.avgCloudMs / stats.avgLocalMs}× longer than one on the phone, " +
                    "and needs a network connection.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PrivacySection(stats: Stats) {
    SectionCard("Privacy") {
        LabeledValue("Links sent to the server", "${stats.sentToCloud}")
        LabeledValue("Parameters cut from links", "${stats.partsStripped}")
        LabeledValue("…that looked personal", "${stats.sensitiveStripped}")
        LabeledValue("Message texts sent", "0")
        HorizontalDivider()
        Text(
            "\"Looked personal\" means an email, phone number, login token or ID. Cut parts never leave the phone.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SourceSection(stats: Stats) {
    SectionCard("How links arrived") {
        stats.bySource.forEach { (s, n) ->
            ShareBar(
                when (s) {
                    CheckSource.SHARE_SHEET -> "Shared from another app"
                    CheckSource.MANUAL -> "Pasted manually"                },
                n, stats.total,
            )
        }
    }
}

@Composable
private fun ShareBar(label: String, count: Int, total: Int) {
    val share = if (total == 0) 0f else count.toFloat() / total
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text("$count · ${pct(share)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { share },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            strokeCap = StrokeCap.Round,
            drawStopIndicator = {},
        )
    }
}

private fun pct(f: Float) = "${(f * 100).toInt()}%"

@Preview(showBackground = true)
@Composable
private fun StatsPreview() {
    UrlCheckerTheme(dynamicColor = false) { StatsScreen(AppState()) {} }
}
