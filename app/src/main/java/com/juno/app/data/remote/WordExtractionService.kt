package com.juno.app.data.remote

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordExtractionService @Inject constructor() {

    private val commonWords = setOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "it",
        "for", "not", "on", "with", "he", "as", "you", "do", "at", "this",
        "but", "his", "by", "from", "they", "we", "say", "her", "she", "or",
        "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know",
        "take", "people", "into", "year", "your", "good", "some", "could",
        "them", "see", "other", "than", "then", "now", "look", "only", "come",
        "its", "over", "think", "also", "back", "after", "use", "two", "how",
        "our", "work", "first", "well", "way", "even", "new", "want", "because",
        "any", "these", "give", "day", "most", "us", "is", "are", "was", "were",
        "been", "being", "has", "had", "did", "does", "done", "doing"
    )

    fun extractKeywords(text: String, maxKeywords: Int = 50): List<String> {
        if (text.isBlank()) return emptyList()

        val words = text.split(Regex("[\\s\\n\\r\\t,;:!?().\\[\\]\"'-]+"))
            .filter { it.isNotBlank() }

        val candidateWords = words.map { word ->
            val cleaned = word.trim()
            CandidateWord(
                original = cleaned,
                length = cleaned.length,
                isCapitalized = cleaned.firstOrNull()?.isUpperCase() == true,
                hasNumbers = cleaned.any { it.isDigit() }
            )
        }

        val scoredWords = candidateWords
            .filter { candidate ->
                candidate.length >= MIN_WORD_LENGTH && 
                !commonWords.contains(candidate.original.lowercase())
            }
            .map { candidate ->
                val score = calculateScore(candidate)
                ScoredWord(candidate.original, score)
            }
            .groupBy { it.word.lowercase() }
            .mapValues { entry -> entry.value.maxByOrNull { it.score }!! }
            .values
            .sortedByDescending { it.score }
            .take(maxKeywords)
            .map { it.word }

        return scoredWords
    }

    private fun calculateScore(candidate: CandidateWord): Double {
        var score = 0.0

        if (candidate.isCapitalized) {
            score += 2.0
        }

        if (candidate.length >= 6) {
            score += 1.5
        } else if (candidate.length >= 4) {
            score += 0.5
        }

        if (candidate.length >= 8) {
            score += 1.0
        }

        if (!candidate.hasNumbers) {
            score += 0.5
        }

        return score
    }

    private data class CandidateWord(
        val original: String,
        val length: Int,
        val isCapitalized: Boolean,
        val hasNumbers: Boolean
    )

    private data class ScoredWord(
        val word: String,
        val score: Double
    )

    companion object {
        private const val MIN_WORD_LENGTH = 3
    }
}
