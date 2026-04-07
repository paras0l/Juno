package com.juno.app.ui.screens.gptworddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.entity.WordEntity
import com.juno.app.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GptWordDetailUiState(
    val isLoading: Boolean = true,
    val word: WordEntity? = null,
    val error: String? = null
)

@HiltViewModel
class GptWordDetailViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wordId: Long = savedStateHandle.get<Long>("wordId") ?: 0

    private val _uiState = MutableStateFlow(GptWordDetailUiState())
    val uiState: StateFlow<GptWordDetailUiState> = _uiState.asStateFlow()

    init {
        loadWord()
    }

    private fun loadWord() {
        viewModelScope.launch {
            try {
                val word = wordRepository.getWordByIdSync(wordId)
                _uiState.value = GptWordDetailUiState(
                    isLoading = false,
                    word = word,
                    error = if (word == null) "词条不存在" else null
                )
            } catch (e: Exception) {
                _uiState.value = GptWordDetailUiState(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    fun markAsLearned() {
        val word = _uiState.value.word ?: return
        viewModelScope.launch {
            wordRepository.updateLearnedStatus(word.id, true)
            _uiState.value = _uiState.value.copy(
                word = word.copy(isLearned = true)
            )
        }
    }
}
