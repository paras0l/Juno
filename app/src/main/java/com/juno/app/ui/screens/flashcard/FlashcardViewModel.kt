package com.juno.app.ui.screens.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.entity.WordEntity
import com.juno.app.domain.repository.ReviewRepository
import com.juno.app.domain.repository.UserRepository
import com.juno.app.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlashcardUiState(
    val isLoading: Boolean = true,
    val words: List<WordEntity> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isComplete: Boolean = false,
    val rememberedCount: Int = 0,
    val forgotCount: Int = 0,
    val error: String? = null
) {
    val currentWord: WordEntity?
        get() = words.getOrNull(currentIndex)

    val progress: Float
        get() = if (words.isEmpty()) 0f else (currentIndex.toFloat() / words.size)

    val totalWords: Int
        get() = words.size
}

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    private var lastLoadedLimit: Int = -1

    init {
        loadWords()
    }

    private fun loadWords() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userProgress = userRepository.getUserProgressSync()
                val limit = userProgress?.dailyGoal ?: 10
                lastLoadedLimit = limit

                val words = wordRepository.getWordsForLearning(
                    maxDifficulty = 5,
                    limit = limit
                ).first()

                if (words.isEmpty()) {
                    val unlearnedWords = wordRepository.getUnlearnedWords(limit).first()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        words = unlearnedWords,
                        isComplete = false, // Never start a fresh load as 'complete'
                        currentIndex = 0,
                        rememberedCount = 0,
                        forgotCount = 0
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        words = words,
                        isComplete = false,
                        currentIndex = 0,
                        rememberedCount = 0,
                        forgotCount = 0
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load words"
                )
            }
        }
    }

    fun loadReviewWords() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userProgress = userRepository.getUserProgressSync()
                val limit = userProgress?.dailyGoal ?: 10
                lastLoadedLimit = limit

                val reviewWords = wordRepository.getRecentlyStudiedWords(limit).first()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    words = reviewWords,
                    isComplete = false,
                    currentIndex = 0,
                    rememberedCount = 0,
                    forgotCount = 0
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load review words"
                )
            }
        }
    }

    fun flipCard() {
        _uiState.value = _uiState.value.copy(isFlipped = !_uiState.value.isFlipped)
    }

    fun checkAndReloadIfNeeded() {
        viewModelScope.launch {
            val userProgress = userRepository.getUserProgressSync()
            val limit = userProgress?.dailyGoal ?: 10
            val currentWords = _uiState.value.words
            val isAtStart = _uiState.value.currentIndex == 0
            
            // Reload if:
            // 1. Goal changed and we are at the start of a session
            // 2. Currently empty (user might have added words in the word library)
            if (!_uiState.value.isLoading && !_uiState.value.isComplete) {
                if ((isAtStart && lastLoadedLimit != limit) || currentWords.isEmpty()) {
                    loadWords()
                }
            }
        }
    }

    fun processRemembered() {
        viewModelScope.launch {
            val currentWord = _uiState.value.currentWord ?: return@launch
            processReview(currentWord.id, remembered = true)
            moveToNextCard(remembered = true)
        }
    }

    fun processForgot() {
        viewModelScope.launch {
            val currentWord = _uiState.value.currentWord ?: return@launch
            processReview(currentWord.id, remembered = false)
            moveToNextCard(remembered = false)
        }
    }

    private suspend fun processReview(wordId: Long, remembered: Boolean) {
        val quality = if (remembered) 4 else 1
        try {
            val oldRecord = reviewRepository.getReviewRecordByWordIdSync(wordId)
            val wasMastered = oldRecord?.repetitions ?: 0 >= 3
            
            val newRecord = reviewRepository.processReview(wordId, quality)
            val isNowMastered = newRecord.repetitions >= 3

            wordRepository.updateLastStudiedDate(wordId, System.currentTimeMillis())
            
            if (remembered) {
                wordRepository.updateLearnedStatus(wordId, true)
                userRepository.incrementWordsLearned()
            }
            userRepository.incrementWordsReviewed()

            if (!wasMastered && isNowMastered) {
                userRepository.incrementMasteredWords()
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = e.message ?: "Failed to process review"
            )
        }
    }

    private fun moveToNextCard(remembered: Boolean) {
        val currentState = _uiState.value
        val nextIndex = currentState.currentIndex + 1

        val newRemembered = currentState.rememberedCount + if (remembered) 1 else 0
        val newForgot = currentState.forgotCount + if (!remembered) 1 else 0

        if (nextIndex >= currentState.words.size) {
            _uiState.value = currentState.copy(
                isComplete = true,
                isFlipped = false,
                rememberedCount = newRemembered,
                forgotCount = newForgot
            )
        } else {
            _uiState.value = currentState.copy(
                currentIndex = nextIndex,
                isFlipped = false,
                rememberedCount = newRemembered,
                forgotCount = newForgot
            )
        }
    }

    fun restart() {
        loadWords()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
