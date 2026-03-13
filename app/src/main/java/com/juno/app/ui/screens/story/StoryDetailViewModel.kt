package com.juno.app.ui.screens.story

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.entity.StoryEntity
import com.juno.app.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StoryDetailUiState(
    val isLoading: Boolean = true,
    val story: StoryEntity? = null,
    val error: String? = null
)

@HiltViewModel
class StoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val storyId: Long = savedStateHandle.get<Long>("storyId") ?: 0L

    private val _uiState = MutableStateFlow(StoryDetailUiState())
    val uiState: StateFlow<StoryDetailUiState> = _uiState.asStateFlow()

    init {
        loadStory()
    }

    private fun loadStory() {
        viewModelScope.launch {
            storyRepository.getStoryById(storyId)
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    null
                )
                .collect { story ->
                    _uiState.value = StoryDetailUiState(
                        isLoading = false,
                        story = story
                    )

                    if (story != null && !story.isRead) {
                        markAsRead()
                    }
                }
        }
    }

    private fun markAsRead() {
        viewModelScope.launch {
            try {
                storyRepository.markStoryAsRead(storyId)
            } catch (e: Exception) {
                // Silently fail - not critical
            }
        }
    }

    fun markAsCompleted() {
        viewModelScope.launch {
            try {
                _uiState.value.story?.let { story ->
                    storyRepository.updateCompletionStatus(storyId, true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
