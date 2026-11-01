package com.example.url_checker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.url_checker.data.AppState
import com.example.url_checker.data.CheckSource
import com.example.url_checker.ui.ConsentScreen
import com.example.url_checker.ui.HomeScreen
import com.example.url_checker.ui.ResultScreen
import com.example.url_checker.ui.SettingsScreen
import com.example.url_checker.ui.StatsScreen
import com.example.url_checker.ui.theme.UrlCheckerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Text shared from SMS / WhatsApp / Mail via the share sheet (only on first creation).
        val sharedText = if (savedInstanceState == null) intent.sharedText() else null

        setContent {
            UrlCheckerTheme {
                val state = remember { AppState() }
                val nav = rememberNavController()
                // A link shared before the consent screen was answered waits until it is.
                var pendingShare by remember { mutableStateOf(sharedText) }

                LaunchedEffect(state.settings.consentAsked) {
                    val text = pendingShare
                    if (state.settings.consentAsked && text != null) {
                        pendingShare = null
                        val results = state.checkText(text, CheckSource.SHARE_SHEET)
                        results.firstOrNull()?.let { nav.navigate("result/${it.id}") }
                    }
                }

                val start = remember { if (state.settings.consentAsked) "home" else "consent" }
                NavHost(nav, startDestination = start) {
                    composable("consent") {
                        ConsentScreen(onChoice = { allow ->
                            state.completeConsent(allow)
                            nav.navigate("home") { popUpTo("consent") { inclusive = true } }
                        })
                    }
                    composable("home") {
                        HomeScreen(
                            state = state,
                            onOpenCheck = { nav.navigate("result/$it") },
                            onOpenSettings = { nav.navigate("settings") },
                            onOpenStats = { nav.navigate("stats") },
                        )
                    }
                    composable("result/{id}") { entry ->
                        val id = entry.arguments?.getString("id")?.toLongOrNull()
                        ResultScreen(
                            check = id?.let(state::find),
                            settings = state.settings,
                            onBack = { nav.popBackStack() },
                            onRetry = state::retryPending,
                            onApproveCloud = { id?.let(state::approveCloud) },
                            onDeclineCloud = { id?.let(state::declineCloud) },
                        )
                    }
                    composable("stats") {
                        StatsScreen(state, onBack = { nav.popBackStack() })
                    }
                    composable("settings") {
                        SettingsScreen(state, onBack = { nav.popBackStack() })
                    }
                }
            }
        }
    }
}

private fun Intent.sharedText(): String? =
    if (action == Intent.ACTION_SEND && type?.startsWith("text/") == true)
        getStringExtra(Intent.EXTRA_TEXT)
    else null
