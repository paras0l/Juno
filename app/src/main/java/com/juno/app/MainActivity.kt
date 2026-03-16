package com.juno.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.juno.app.data.local.PreferencesManager
import com.juno.app.service.FloatingWindowService
import com.juno.app.ui.navigation.JunoNavHost
import com.juno.app.ui.theme.AppTheme
import com.juno.app.ui.theme.JunoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            val themeMode by preferencesManager.themeMode.collectAsState(initial = "light")
            val onboardingCompleted by preferencesManager.onboardingCompleted.collectAsState(initial = true)
            val theme = when (themeMode) {
                "dark" -> AppTheme.DARK
                "morandi" -> AppTheme.MORANDI
                "minimalist" -> AppTheme.MINIMALIST
                else -> AppTheme.LIGHT
            }

            JunoTheme(theme = theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    JunoNavHost(
                        navController = navController,
                        preferencesManager = preferencesManager,
                        onboardingCompleted = onboardingCompleted
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return

        when (intent.action) {
            Intent.ACTION_SEND -> {
                when (intent.type) {
                    "text/plain" -> {
                        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                        if (!sharedText.isNullOrBlank()) {
                            if (sharedText.startsWith("http://") || sharedText.startsWith("https://")) {
                                handleSharedUrl(sharedText)
                            } else {
                                handleSharedText(sharedText)
                            }
                        }
                    }
                }
            }
            Intent.ACTION_VIEW -> {
                val uri = intent.data
                if (uri != null) {
                    handleUri(uri)
                }
            }
            FloatingWindowService.ACTION_START -> {
                startFloatingService()
            }
        }
    }

    private fun handleSharedText(text: String) {
        Toast.makeText(this, "Received text: ${text.take(50)}", Toast.LENGTH_SHORT).show()
    }

    private fun handleSharedUrl(url: String) {
        val extractedUrl = extractUrl(url)
        if (extractedUrl != null) {
            Toast.makeText(this, "Received URL: $extractedUrl", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Invalid URL received", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleUri(uri: Uri) {
        val url = uri.toString()
        Toast.makeText(this, "Received URI: $url", Toast.LENGTH_SHORT).show()
    }

    private fun extractUrl(text: String): String? {
        val urlPattern = Regex("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+")
        return urlPattern.find(text)?.value
    }

    private fun startFloatingService() {
        val serviceIntent = Intent(this, FloatingWindowService::class.java).apply {
            action = FloatingWindowService.ACTION_START
        }
        startForegroundService(serviceIntent)
    }
}
