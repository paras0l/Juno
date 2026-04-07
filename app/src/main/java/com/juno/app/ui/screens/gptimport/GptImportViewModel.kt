package com.juno.app.ui.screens.gptimport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.remote.GptWordsImportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GptImportUiState(
    val isImporting: Boolean = false,
    val totalCount: Int = 0,
    val importedCount: Int = 0,
    val duplicateCount: Int = 0,
    val success: Boolean? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class GptImportViewModel @Inject constructor(
    private val gptWordsImportService: GptWordsImportService
) : ViewModel() {

    private val _uiState = MutableStateFlow(GptImportUiState())
    val uiState: StateFlow<GptImportUiState> = _uiState.asStateFlow()

    fun startImport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, success = null, errorMessage = null)
            
            val result = gptWordsImportService.importWords()
            
            _uiState.value = _uiState.value.copy(
                isImporting = false,
                totalCount = result.totalCount,
                importedCount = result.importedCount,
                duplicateCount = result.duplicateCount,
                success = result.success,
                errorMessage = result.errorMessage
            )
        }
    }

    fun reset() {
        _uiState.value = GptImportUiState()
    }
}
