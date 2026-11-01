package com.example.url_checker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.url_checker.data.AppState
import com.example.url_checker.ui.theme.UrlCheckerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(state: AppState, onBack: () -> Unit) {
    val s = state.settings
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            SectionHeader("Privacy")
            ListItem(
                headlineContent = { Text("Cloud second opinion") },
                supportingContent = {
                    Text(
                        "When the on-device model is unsure, or the link is shortened, send the link " +
                            "(without parameters) to our server. Message text is never sent.",
                    )
                },
                trailingContent = {
                    Switch(checked = s.cloudConsent, onCheckedChange = {
                        state.settings = s.copy(cloudConsent = it)
                    })
                },
            )
            HorizontalDivider()

            SectionHeader("Detection")
            ListItem(
                headlineContent = { Text("On-device model") },
                supportingContent = { Text("urlbert-tiny · ONNX Runtime") },
            )
            ListItem(
                headlineContent = { Text("Confidence thresholds") },
                supportingContent = {
                    Text(
                        "Safe below ${(s.lowThreshold * 100).toInt()}%, phishing above " +
                            "${(s.highThreshold * 100).toInt()}%. Everything in between goes to the cloud. " +
                            "Chosen on a validation set to keep the on-device error under 1%.",
                    )
                },
            )
            HorizontalDivider()

            SectionHeader("Managed devices")
            ListItem(
                headlineContent = { Text("Check links when opened") },
                supportingContent = {
                    Text(
                        "Uses an Accessibility Service to catch links before the page loads. " +
                            "Meant for company phones configured via MDM.",
                    )
                },
                trailingContent = { Switch(checked = false, onCheckedChange = null, enabled = false) },
            )
            HorizontalDivider()

            SectionHeader("Prototype")
            ListItem(
                headlineContent = { Text("Simulate offline") },
                supportingContent = { Text("Pretend there's no network: cloud checks are deferred.") },
                trailingContent = {
                    Switch(checked = s.simulateOffline, onCheckedChange = {
                        state.settings = s.copy(simulateOffline = it)
                        if (!it) state.retryPending()
                    })
                },
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Row(Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, bottom = 4.dp)) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    UrlCheckerTheme(dynamicColor = false) { SettingsScreen(AppState()) {} }
}
