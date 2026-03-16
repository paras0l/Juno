package com.juno.app.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.entity.ReviewRecordEntity
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

data class ReviewUiState(
    val isLoading: Boolean = true,
    val reviews: List<Pair<WordEntity, ReviewRecordEntity>> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isComplete: Boolean = false,
    val againCount: Int = 0,
    val hardCount: Int = 0,
    val goodCount: Int = 0,
    val easyCount: Int = 0,
    val error: String? = null
) {
    val currentWord: WordEntity?
        get() = reviews.getOrNull(currentIndex)?.first

    val progress: Float
        get() = if (reviews.isEmpty()) 0f else (currentIndex.toFloat() / reviews.size)

    val totalReviews: Int
        get() = reviews.size

    val completedCount: Int
        get() = againCount + hardCount + goodCount + easyCount
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        loadDueReviews()
    }

    private fun loadDueReviews() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val dueRecords = reviewRepository.getDueReviewsLimited(20).first()
                
                if (dueRecords.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        reviews = emptyList(),
                        isComplete = false  // Not complete, just nothing to review
                    )
                    return@launch
                }

                val wordsWithReviews = dueRecords.mapNotNull { record ->
                    val word = wordRepository.getWordByIdSync(record.wordId)
                    if (word != null) {
                        word to record
                    } else {
                        null
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    reviews = wordsWithReviews,
                    isComplete = wordsWithReviews.isEmpty()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load reviews"
                )
            }
        }
    }

    fun flipCard() {
        _uiState.value = _uiState.value.copy(isFlipped = !_uiState.value.isFlipped)
    }

    fun processReview(quality: Int) {
        viewModelScope.launch {
            val currentPair = _uiState.value.reviews.getOrNull(_uiState.value.currentIndex)
                ?: return@launch

            try {
                reviewRepository.processReview(currentPair.first.id, quality)
                userRepository.incrementWordsReviewed()

                when (quality) {
                    1 -> _uiState.value = _uiState.value.copy(againCount = _uiState.value.againCount + 1)
                    3 -> _uiState.value = _uiState.value.copy(hardCount = _uiState.value.hardCount + 1)
                    4 -> _uiState.value = _uiState.value.copy(goodCount = _uiState.value.goodCount + 1)
                    5 -> _uiState.value = _uiState.value.copy(easyCount = _uiState.value.easyCount + 1)
                }

                moveToNextCard()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to process review"
                )
            }
        }
    }

    fun processAgain() = processReview(1)
    fun processHard() = processReview(3)
    fun processGood() = processReview(4)
    fun processEasy() = processReview(5)

    private fun moveToNextCard() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentIndex + 1

        if (nextIndex >= currentState.reviews.size) {
            _uiState.value = currentState.copy(
                isComplete = true,
                isFlipped = false
            )
        } else {
            _uiState.value = currentState.copy(
                currentIndex = nextIndex,
                isFlipped = false
            )
        }
    }

    fun restart() {
        _uiState.value = ReviewUiState()
        loadDueReviews()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
