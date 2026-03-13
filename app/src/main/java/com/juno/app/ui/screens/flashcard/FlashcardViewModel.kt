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

    init {
        loadWords()
    }

    private fun loadWords() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val words = wordRepository.getWordsForLearning(
                    maxDifficulty = 5,
                    limit = 20
                ).first()

                if (words.isEmpty()) {
                    val unlearnedWords = wordRepository.getUnlearnedWords(20).first()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        words = unlearnedWords,
                        isComplete = unlearnedWords.isEmpty()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        words = words
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

    fun flipCard() {
        _uiState.value = _uiState.value.copy(isFlipped = !_uiState.value.isFlipped)
    }

    fun processRemembered() {
        viewModelScope.launch {
            val currentWord = _uiState.value.currentWord ?: return@launch
            processReview(currentWord.id, remembered = true)
            moveToNextCard()
        }
    }

    fun processForgot() {
        viewModelScope.launch {
            val currentWord = _uiState.value.currentWord ?: return@launch
            processReview(currentWord.id, remembered = false)
            moveToNextCard()
        }
    }

    private suspend fun processReview(wordId: Long, remembered: Boolean) {
        val quality = if (remembered) 4 else 1
        try {
            reviewRepository.processReview(wordId, quality)
            if (remembered) {
                wordRepository.updateLearnedStatus(wordId, true)
                userRepository.incrementWordsLearned()
            }
            userRepository.incrementWordsReviewed()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = e.message ?: "Failed to process review"
            )
        }
    }

    private fun moveToNextCard() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentIndex + 1

        if (nextIndex >= currentState.words.size) {
            _uiState.value = currentState.copy(
                isComplete = true,
                isFlipped = false
            )
        } else {
            _uiState.value = currentState.copy(
                currentIndex = nextIndex,
                isFlipped = false,
                rememberedCount = if (currentState.isFlipped) {
                    currentState.rememberedCount + 1
                } else {
                    currentState.rememberedCount
                },
                forgotCount = if (!currentState.isFlipped) {
                    currentState.forgotCount + 1
                } else {
                    currentState.forgotCount
                }
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
