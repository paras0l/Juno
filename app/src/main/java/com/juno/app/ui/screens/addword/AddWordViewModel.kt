package com.juno.app.ui.screens.addword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.entity.WordEntity
import com.juno.app.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddWordUiState(
    val word: String = "",
    val phonetic: String = "",
    val meaning: String = "",
    val example: String = "",
    val translation: String = "",
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val wordError: String? = null,
    val meaningError: String? = null,
    val saveSuccess: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
        get() = word.isNotBlank() && meaning.isNotBlank()
}

@HiltViewModel
class AddWordViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wordId: Long = savedStateHandle.get<Long>("wordId") ?: 0L

    private val _uiState = MutableStateFlow(AddWordUiState())
    val uiState: StateFlow<AddWordUiState> = _uiState.asStateFlow()

    init {
        if (wordId > 0) {
            loadWord(wordId)
        }
    }

    private fun loadWord(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val word = wordRepository.getWordByIdSync(id)
                if (word != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditMode = true,
                            word = word.word,
                            phonetic = word.phonetic ?: "",
                            meaning = word.meaning,
                            example = word.example ?: "",
                            translation = word.translation ?: ""
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Word not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load word"
                    )
                }
            }
        }
    }

    fun onWordChange(value: String) {
        _uiState.update {
            it.copy(
                word = value,
                wordError = if (value.isBlank()) "单词不能为空" else null
            )
        }
    }

    fun onPhoneticChange(value: String) {
        _uiState.update { it.copy(phonetic = value) }
    }

    fun onMeaningChange(value: String) {
        _uiState.update {
            it.copy(
                meaning = value,
                meaningError = if (value.isBlank()) "释义不能为空" else null
            )
        }
    }

    fun onExampleChange(value: String) {
        _uiState.update { it.copy(example = value) }
    }

    fun onTranslationChange(value: String) {
        _uiState.update { it.copy(translation = value) }
    }

    fun saveWord() {
        val currentState = _uiState.value

        var hasError = false
        if (currentState.word.isBlank()) {
            _uiState.update { it.copy(wordError = "单词不能为空") }
            hasError = true
        }
        if (currentState.meaning.isBlank()) {
            _uiState.update { it.copy(meaningError = "释义不能为空") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val wordEntity = WordEntity(
                    id = if (currentState.isEditMode) wordId else 0,
                    word = currentState.word.trim(),
                    phonetic = currentState.phonetic.trim().ifBlank { null },
                    meaning = currentState.meaning.trim(),
                    example = currentState.example.trim().ifBlank { null },
                    translation = currentState.translation.trim().ifBlank { null }
                )

                if (currentState.isEditMode) {
                    wordRepository.updateWord(wordEntity)
                } else {
                    wordRepository.insertWord(wordEntity)
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save word"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
