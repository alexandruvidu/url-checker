package com.example.url_checker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.url_checker.data.AppState
import com.example.url_checker.data.CheckSource
import com.example.url_checker.ui.HomeScreen
import com.example.url_checker.ui.ResultScreen
import com.example.url_checker.ui.SettingsScreen
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

                LaunchedEffect(Unit) {
                    if (sharedText != null) {
                        val results = state.checkText(sharedText, CheckSource.SHARE_SHEET)
                        results.firstOrNull()?.let { nav.navigate("result/${it.id}") }
                    }
                }

                NavHost(nav, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            state = state,
                            onOpenCheck = { nav.navigate("result/$it") },
                            onOpenSettings = { nav.navigate("settings") },
                        )
                    }
                    composable("result/{id}") { entry ->
                        val id = entry.arguments?.getString("id")?.toLongOrNull()
                        ResultScreen(
                            check = id?.let(state::find),
                            settings = state.settings,
                            onBack = { nav.popBackStack() },
                            onRetry = state::retryPending,
                        )
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
