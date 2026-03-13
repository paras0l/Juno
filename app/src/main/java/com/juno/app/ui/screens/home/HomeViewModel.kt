package com.juno.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.entity.UserProgressEntity
import com.juno.app.domain.repository.ReviewRepository
import com.juno.app.domain.repository.UserRepository
import com.juno.app.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val userProgress: UserProgressEntity? = null,
    val totalWordsLearned: Int = 0,
    val dueReviewsCount: Int = 0,
    val dailyGoal: Int = 10,
    val wordsLearnedToday: Int = 0,
    val wordsReviewedToday: Int = 0,
    val currentStreak: Int = 0,
    val masteredWords: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val wordRepository: WordRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            userRepository.initializeProgress()
        }

        viewModelScope.launch {
            combine(
                userRepository.getUserProgress(),
                wordRepository.getLearnedWordsCount(),
                reviewRepository.getDueReviewsCount()
            ) { progress, learnedWords, dueReviews ->
                HomeUiState(
                    isLoading = false,
                    userProgress = progress,
                    totalWordsLearned = learnedWords,
                    dueReviewsCount = dueReviews,
                    dailyGoal = progress?.dailyGoal ?: 10,
                    wordsLearnedToday = progress?.wordsLearnedToday ?: 0,
                    wordsReviewedToday = progress?.wordsReviewedToday ?: 0,
                    currentStreak = progress?.currentStreak ?: 0,
                    masteredWords = progress?.masteredWords ?: 0
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                HomeUiState()
            ).collect { state ->
                _uiState.value = state
            }
        }
    }

    fun refreshData() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadHomeData()
    }
}
