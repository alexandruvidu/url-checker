package com.example.url_checker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.url_checker.data.AppState
import com.example.url_checker.data.CheckSource
import com.example.url_checker.data.DecidedBy
import com.example.url_checker.data.FakeUrlChecker
import com.example.url_checker.data.Verdict
import com.example.url_checker.ui.components.CheckRow
import com.example.url_checker.ui.theme.UrlCheckerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: AppState,
    onOpenCheck: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStats: () -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var noUrlFound by remember { mutableStateOf(false) }
    val pending = state.history.count { it.decidedBy == DecidedBy.CLOUD_PENDING_OFFLINE }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Link Guard") },
                actions = {
                    IconButton(onClick = onOpenStats) { Icon(Icons.AutoMirrored.Filled.List, "Statistics") }
                    IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, "Settings") }
                },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Paste a message or a link. Only the link is checked — the message text never leaves your phone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it; noUrlFound = false },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Message or URL") },
                        placeholder = { Text("e.g. Coletul tău e blocat: http://...") },
                        minLines = 2,
                        maxLines = 5,
                        isError = noUrlFound,
                        supportingText = if (noUrlFound) {
                            { Text("No link found in this text") }
                        } else null,
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val results = state.checkText(input, CheckSource.MANUAL)
                            if (results.isEmpty()) noUrlFound = true
                            else {
                                input = ""
                                onOpenCheck(results.first().id)
                            }
                        },
                        enabled = input.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.Search, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Check link")
                    }
                }
            }

            item { StatusCard(state, pending, onOpenStats) }

            item {
                Row(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Recent checks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    if (state.history.isNotEmpty()) {
                        TextButton(onClick = state::clearHistory) { Text("Clear") }
                    }
                }
            }

            if (state.history.isEmpty()) {
                item {
                    Text(
                        "Nothing checked yet. Tip: in SMS, WhatsApp or Mail, long-press a message → Share → Link Guard.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(state.history, key = { it.id }) { check ->
                CheckRow(check) { onOpenCheck(check.id) }
                HorizontalDivider(Modifier.padding(start = 72.dp))
            }
        }
    }
}

@Composable
private fun StatusCard(state: AppState, pending: Int, onOpenStats: () -> Unit) {
    val total = state.history.size
    val phishing = state.history.count { it.verdict == Verdict.PHISHING }
    val cloud = state.history.count { it.decidedBy == DecidedBy.CLOUD }
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                Stat("$total", "checked")
                Stat("$phishing", "phishing")
                Stat(if (total == 0) "–" else "${cloud * 100 / total}%", "sent to cloud")
            }
            TextButton(onClick = onOpenStats, modifier = Modifier.align(Alignment.End)) {
                Text("See all statistics")
            }
            if (state.settings.simulateOffline || pending > 0) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, null, Modifier.padding(end = 8.dp))
                    Text(
                        when {
                            state.settings.simulateOffline && pending > 0 ->
                                "Offline. $pending link(s) waiting for the cloud check."
                            state.settings.simulateOffline -> "Offline. Only on-device checks are available."
                            else -> "$pending link(s) still need a cloud check."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    if (!state.settings.simulateOffline && pending > 0) {
                        TextButton(onClick = state::retryPending) { Text("Retry") }
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    UrlCheckerTheme(dynamicColor = false) { HomeScreen(AppState(), {}, {}, {}) }
}
