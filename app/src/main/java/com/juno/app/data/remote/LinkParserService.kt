package com.juno.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkParserService @Inject constructor() {

    data class ParsedContent(
        val title: String,
        val text: String,
        val sourceUrl: String,
        val extractedWords: List<String>
    )

    /**
     * Parse a URL and extract its text content using Jsoup
     * @param url The URL to parse
     * @return Result containing ParsedContent on success
     */
    suspend fun parseUrl(url: String): Result<ParsedContent> = withContext(Dispatchers.IO) {
        try {
            val normalizedUrl = normalizeUrl(url)
            
            val doc: Document = Jsoup.connect(normalizedUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get()

            val title = extractTitle(doc)
            val text = extractTextContent(doc)
            val extractedWords = extractKeywords(text)

            Result.success(
                ParsedContent(
                    title = title,
                    text = text,
                    sourceUrl = normalizedUrl,
                    extractedWords = extractedWords
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extract YouTube video subtitles/captions
     * @param videoUrl YouTube video URL (supports various formats)
     * @return Result containing subtitle text on success
     */
    suspend fun getYouTubeSubtitles(videoUrl: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val videoId = extractYouTubeVideoId(videoUrl)
                ?: return@withContext Result.failure(IllegalArgumentException("Invalid YouTube URL"))

            // Try to get subtitles using YouTube's caption API approach
            // Note: This is a simplified implementation. For production, 
            // you might want to use a more robust library like youtube-transcript-api
            val subtitles = fetchYouTubeCaptions(videoId)
            
            Result.success(subtitles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun normalizeUrl(url: String): String {
        return if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
    }

    private fun extractTitle(doc: Document): String {
        // Try Open Graph title first, then fallback to regular title
        return doc.select("meta[property=og:title]").attr("content").ifBlank {
            doc.select("meta[name=twitter:title]").attr("content").ifBlank {
                doc.title()
            }
        }
    }

    private fun extractTextContent(doc: Document): String {
        // Remove unwanted elements
        val clone = doc.clone()
        
        clone.select("script, style, nav, header, footer, aside, .advertisement, .ad, .sidebar, .comments, .social-share, .related-posts").remove()
        
        // Get text from main content areas
        val mainContent = clone.select("article, main, .content, .post-content, .article-content, #content, .entry-content")
        
        return if (mainContent.isNotEmpty()) {
            mainContent.text()
        } else {
            clone.body().text()
        }
    }

    private fun extractKeywords(text: String): List<String> {
        val wordExtractionService = WordExtractionService()
        return wordExtractionService.extractKeywords(text)
    }

    private fun extractYouTubeVideoId(url: String): String? {
        val patterns = listOf(
            Regex("(?:youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/embed/)([a-zA-Z0-9_-]{11})"),
            Regex("youtube\\.com/v/([a-zA-Z0-9_-]{11})"),
            Regex("youtube\\.com/shorts/([a-zA-Z0-9_-]{11})")
        )

        for (pattern in patterns) {
            val match = pattern.find(url)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }

    private fun fetchYouTubeCaptions(videoId: String): String {
        // This is a placeholder implementation
        // In a production app, you would use a proper YouTube Data API or caption extraction library
        // For now, we'll fetch the video page and try to find caption tracks
        
        val url = URL("https://www.youtube.com/watch?v=$videoId")
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val pageContent = reader.readText()
            reader.close()

            // Try to find caption tracks in the page
            val captionPattern = Regex("\"captionTracks\":\\s*\\[([^\\]]+)\\]")
            val match = captionPattern.find(pageContent)
            
            if (match != null) {
                // Parse caption tracks and return the first English caption if available
                // This is simplified - production code would need proper JSON parsing
                return "Captions found but require API key for retrieval. Consider using YouTube Data API."
            }
            
            return "No captions available for this video."
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        // Common domains for content extraction
        val SUPPORTED_DOMAINS = listOf(
            "medium.com",
            "dev.to",
            "github.com",
            "stackoverflow.com",
            "wikipedia.org",
            "news.ycombinator.com",
            "reddit.com"
        )
    }
}
