package com.example.url_checker.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.url_checker.data.FakeUrlChecker
import com.example.url_checker.ui.components.SanitizedUrlDiff
import com.example.url_checker.ui.components.SectionCard
import com.example.url_checker.ui.theme.UrlCheckerTheme

private const val EXAMPLE_URL = "https://fan-colet.xyz/track?awb=88213&phone=0722123456&utm_source=sms#pay"

/**
 * First-run consent. Nothing is sent anywhere until the user answers.
 * [onChoice] gets true for "allow cloud checks", false for "on-device only".
 */
@Composable
fun ConsentScreen(onChoice: (allowCloud: Boolean) -> Unit) {
    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                Icons.Filled.Lock, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Text("Before you start", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Link Guard checks links for phishing. Here is exactly what happens to your data.",
                style = MaterialTheme.typography.bodyLarge,
            )

            Point(
                Icons.Filled.Search,
                "Checked on your phone first",
                "Every link goes through a small AI model that runs on your phone. Most links are decided here, " +
                    "and nothing leaves the device.",
            )
            Point(
                Icons.AutoMirrored.Filled.Send,
                "Sometimes, a second opinion",
                "If the model is unsure, or the link is shortened (bit.ly…), Link Guard can ask our server. " +
                    "It checks threat lists, follows redirects and looks up the domain's age.",
            )
            Point(
                Icons.Filled.Close,
                "Never sent",
                "The message the link came in, your contacts, and the link's parameters, which often hide " +
                    "phone numbers, emails or login tokens.",
            )

            SectionCard("Example: what the server would get") {
                SanitizedUrlDiff(EXAMPLE_URL, FakeUrlChecker.sanitize(EXAMPLE_URL), sentLabel = "Would send")
            }

            Text(
                "You'll see this preview and approve it before each cloud check. " +
                    "You can change your choice any time in Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(4.dp))
            Button(onClick = { onChoice(true) }, modifier = Modifier.fillMaxWidth()) {
                Text("Allow cloud checks")
            }
            OutlinedButton(onClick = { onChoice(false) }, modifier = Modifier.fillMaxWidth()) {
                Text("On-device only")
            }
        }
    }
}

@Composable
private fun Point(icon: ImageVector, title: String, body: String) {
    Row {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConsentPreview() {
    UrlCheckerTheme(dynamicColor = false) { ConsentScreen {} }
}
