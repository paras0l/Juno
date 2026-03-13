package com.juno.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.entity.UserProgressEntity
import com.juno.app.domain.repository.StoryRepository
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

data class ProfileUiState(
    val isLoading: Boolean = true,
    val userProgress: UserProgressEntity? = null,
    val totalWordsLearned: Int = 0,
    val wordsReviewedToday: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completedStories: Int = 0,
    val masteredWords: Int = 0,
    val dailyGoal: Int = 10
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val wordRepository: WordRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            combine(
                userRepository.getUserProgress(),
                wordRepository.getLearnedWordsCount(),
                storyRepository.getCompletedStoriesCount()
            ) { progress, learnedWords, storiesCount ->
                ProfileUiState(
                    isLoading = false,
                    userProgress = progress,
                    totalWordsLearned = learnedWords,
                    wordsReviewedToday = progress?.wordsReviewedToday ?: 0,
                    currentStreak = progress?.currentStreak ?: 0,
                    longestStreak = progress?.longestStreak ?: 0,
                    completedStories = storiesCount,
                    masteredWords = progress?.masteredWords ?: 0,
                    dailyGoal = progress?.dailyGoal ?: 10
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                ProfileUiState()
            ).collect { state ->
                _uiState.value = state
            }
        }
    }

    fun refreshProfile() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadProfile()
    }
}
