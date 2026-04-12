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
    val definitions: String = "",
    val sentence: String = "",
    val sentenceTranslation: String = "",
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val wordError: String? = null,
    val definitionsError: String? = null,
    val saveSuccess: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
        get() = word.isNotBlank() && definitions.isNotBlank()
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
                            definitions = word.definitions ?: "",
                            sentence = word.sentence ?: "",
                            sentenceTranslation = word.sentenceTranslation ?: ""
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

    fun onDefinitionsChange(value: String) {
        _uiState.update {
            it.copy(
                definitions = value,
                definitionsError = if (value.isBlank()) "释义不能为空" else null
            )
        }
    }

    fun onSentenceChange(value: String) {
        _uiState.update { it.copy(sentence = value) }
    }

    fun onSentenceTranslationChange(value: String) {
        _uiState.update { it.copy(sentenceTranslation = value) }
    }

    fun saveWord() {
        val currentState = _uiState.value

        var hasError = false
        if (currentState.word.isBlank()) {
            _uiState.update { it.copy(wordError = "单词不能为空") }
            hasError = true
        }
        if (currentState.definitions.isBlank()) {
            _uiState.update { it.copy(definitionsError = "释义不能为空") }
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
                    definitions = currentState.definitions.trim().ifBlank { null },
                    sentence = currentState.sentence.trim().ifBlank { null },
                    sentenceTranslation = currentState.sentenceTranslation.trim().ifBlank { null }
                )

                if (currentState.isEditMode) {
                    wordRepository.updateWord(wordEntity)
                } else {
                    // Check for duplicates
                    val existingWord = wordRepository.getWordByText(wordEntity.word)
                    if (existingWord != null) {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                wordError = "该单词已存在于单词本中"
                            )
                        }
                        return@launch
                    }
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
