package com.example.url_checker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.url_checker.data.DecidedBy
import com.example.url_checker.data.FakeUrlChecker
import com.example.url_checker.data.UrlCheck
import com.example.url_checker.data.Verdict
import com.example.url_checker.ui.theme.LocalVerdictColors
import java.text.DateFormat
import java.util.Date

data class VerdictStyle(val label: String, val icon: ImageVector, val container: Color, val content: Color)

@Composable
fun verdictStyle(check: UrlCheck): VerdictStyle {
    val c = LocalVerdictColors.current
    return when {
        check.decidedBy == DecidedBy.CLOUD_PENDING_OFFLINE ->
            VerdictStyle("Check pending", Icons.Filled.Info, c.uncertain, c.onUncertain)
        check.verdict == Verdict.SAFE -> VerdictStyle("Looks safe", Icons.Filled.CheckCircle, c.safe, c.onSafe)
        check.verdict == Verdict.PHISHING -> VerdictStyle("Phishing", Icons.Filled.Warning, c.phishing, c.onPhishing)
        else -> VerdictStyle("Uncertain", Icons.Filled.Info, c.uncertain, c.onUncertain)
    }
}

@Composable
fun VerdictChip(check: UrlCheck, modifier: Modifier = Modifier) {
    val s = verdictStyle(check)
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(s.container)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(s.icon, null, tint = s.content, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(s.label, color = s.content, style = MaterialTheme.typography.labelMedium)
    }
}

fun decidedByLabel(d: DecidedBy) = when (d) {
    DecidedBy.ON_DEVICE -> "On device"
    DecidedBy.CLOUD -> "Cloud"
    DecidedBy.CLOUD_PENDING_OFFLINE -> "Waiting for network"
    DecidedBy.CLOUD_SKIPPED_NO_CONSENT -> "On device (cloud off)"
}

fun formatTime(millis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(millis))

@Composable
fun CheckRow(check: UrlCheck, onClick: () -> Unit) {
    val s = verdictStyle(check)
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = CircleShape, color = s.container, modifier = Modifier.size(40.dp)) {
            Icon(s.icon, s.label, tint = s.content, modifier = Modifier.padding(8.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                FakeUrlChecker.host(check.originalUrl),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(
                check.originalUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${decidedByLabel(check.decidedBy)} · ${formatTime(check.checkedAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            s.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = s.content,
        )
    }
}

@Composable
fun MonoText(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
fun LabeledValue(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
