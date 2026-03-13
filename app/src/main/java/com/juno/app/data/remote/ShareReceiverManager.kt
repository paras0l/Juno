package com.juno.app.data.remote

import android.content.Intent
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareReceiverManager @Inject constructor() {

    data class SharedContent(
        val type: ContentType,
        val text: String,
        val url: String? = null,
        val title: String? = null
    )

    enum class ContentType {
        URL,
        TEXT,
        UNKNOWN
    }

    fun parseIntent(intent: Intent?): SharedContent? {
        if (intent == null) return null

        return when (intent.action) {
            Intent.ACTION_SEND -> handleSendAction(intent)
            else -> null
        }
    }

    private fun handleSendAction(intent: Intent): SharedContent? {
        val type = intent.type ?: return null

        return when {
            type.startsWith("text/") -> handleTextShare(intent)
            else -> null
        }
    }

    private fun handleTextShare(intent: Intent): SharedContent? {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)

        if (sharedText.isNullOrBlank()) return null

        val url = extractUrl(sharedText)

        return if (url != null) {
            SharedContent(
                type = ContentType.URL,
                text = sharedText,
                url = url,
                title = sharedSubject
            )
        } else {
            SharedContent(
                type = ContentType.TEXT,
                text = sharedText,
                title = sharedSubject
            )
        }
    }

    private fun extractUrl(text: String): String? {
        val urlPattern = Regex(
            "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)",
            RegexOption.IGNORE_CASE
        )
        
        return urlPattern.find(text)?.groupValues?.getOrNull(1)
    }

    fun isValidShareIntent(intent: Intent?): Boolean {
        return intent?.action == Intent.ACTION_SEND && 
               (intent.type?.startsWith("text/") == true)
    }
}
