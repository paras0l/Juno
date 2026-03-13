package com.juno.app.ui.screens.wordlist

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.entity.WordEntity
import com.juno.app.data.remote.ExcelImportService
import com.juno.app.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WordListUiState(
    val isLoading: Boolean = true,
    val words: List<WordEntity> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val wordToDelete: WordEntity? = null,
    val isImporting: Boolean = false,
    val importResult: ImportResult? = null
)

data class ImportResult(
    val success: Boolean,
    val importedCount: Int = 0,
    val message: String = ""
)

@HiltViewModel
class WordListViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val excelImportService: ExcelImportService
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _wordToDelete = MutableStateFlow<WordEntity?>(null)
    val wordToDelete: StateFlow<WordEntity?> = _wordToDelete.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val words: StateFlow<List<WordEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                wordRepository.getAllWords()
            } else {
                wordRepository.searchWords(query)
            }
        }
        .map { words ->
            words.sortedByDescending { it.createdAt }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val isLoading: StateFlow<Boolean> = words
        .map { false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun showDeleteConfirmation(word: WordEntity) {
        _wordToDelete.value = word
        _showDeleteDialog.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = false
        _wordToDelete.value = null
    }

    fun confirmDelete() {
        val word = _wordToDelete.value ?: return
        viewModelScope.launch {
            try {
                wordRepository.deleteWord(word)
                _showDeleteDialog.value = false
                _wordToDelete.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete word"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun importFromExcel(uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            _importResult.value = null
            
            try {
                val result = excelImportService.importWords(uri)
                
                if (result.success && result.words.isNotEmpty()) {
                    wordRepository.insertWords(result.words)
                    _importResult.value = ImportResult(
                        success = true,
                        importedCount = result.importedCount,
                        message = "成功导入 ${result.importedCount} 个单词"
                    )
                } else {
                    _importResult.value = ImportResult(
                        success = false,
                        message = result.errorMessage ?: "导入失败"
                    )
                }
            } catch (e: Exception) {
                _importResult.value = ImportResult(
                    success = false,
                    message = "导入失败: ${e.message}"
                )
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun clearImportResult() {
        _importResult.value = null
    }
}
