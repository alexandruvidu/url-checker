package com.example.url_checker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** Verdict colours stay fixed (not dynamic) so "safe" is always green and "phishing" always red. */
@Immutable
data class VerdictColors(
    val safe: Color, val onSafe: Color,
    val phishing: Color, val onPhishing: Color,
    val uncertain: Color, val onUncertain: Color,
)

private val LightVerdict = VerdictColors(
    safe = Color(0xFFD6F5DD), onSafe = Color(0xFF0D5A23),
    phishing = Color(0xFFFFDAD6), onPhishing = Color(0xFF93000A),
    uncertain = Color(0xFFFFEFC7), onUncertain = Color(0xFF6B4E00),
)
private val DarkVerdict = VerdictColors(
    safe = Color(0xFF1C4A2A), onSafe = Color(0xFFA6EDB6),
    phishing = Color(0xFF5C1A17), onPhishing = Color(0xFFFFB4AB),
    uncertain = Color(0xFF4F3D0B), onUncertain = Color(0xFFF7D98A),
)

val LocalVerdictColors = staticCompositionLocalOf { LightVerdict }

@Composable
fun UrlCheckerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> darkColorScheme(primary = Color(0xFF9FCAFF), secondary = Color(0xFFBBC7DB))
        else -> lightColorScheme(primary = Color(0xFF1B5FA8), secondary = Color(0xFF535F70))
    }
    androidx.compose.runtime.CompositionLocalProvider(
        LocalVerdictColors provides if (darkTheme) DarkVerdict else LightVerdict,
    ) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
