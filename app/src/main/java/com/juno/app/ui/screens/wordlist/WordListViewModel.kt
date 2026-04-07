package com.juno.app.ui.screens.wordlist

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
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
    private val excelImportService: ExcelImportService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val filter: String = savedStateHandle.get<String>("filter")?.takeIf { it.isNotBlank() } ?: ""

    val screenTitle: String = when (filter) {
        "learned" -> "已学单词"
        "mastered" -> "已掌握单词"
        else -> "单词本"
    }

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
            when {
                query.isNotBlank() && filter == "learned" -> wordRepository.searchLearnedWords(query)
                query.isNotBlank() && filter == "mastered" -> wordRepository.searchMasteredWords(query)
                query.isNotBlank() -> wordRepository.searchWords(query)
                filter == "learned" -> wordRepository.getLearnedWords()
                filter == "mastered" -> wordRepository.getMasteredWords()
                else -> wordRepository.getAllWords()
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
                    // Filter out already existing words
                    val existingWords = wordRepository.getAllWordsList().map { it.lowercase() }.toSet()
                    val newWords = result.words.filter { it.word.lowercase() !in existingWords }
                    
                    if (newWords.isNotEmpty()) {
                        wordRepository.insertWords(newWords)
                    }
                    
                    val skippedCount = result.words.size - newWords.size
                    val message = if (skippedCount > 0) {
                        "成功导入 ${newWords.size} 个单词 (跳过 ${skippedCount} 个已存在单词)"
                    } else {
                        "成功导入 ${newWords.size} 个单词"
                    }
                    
                    _importResult.value = ImportResult(
                        success = true,
                        importedCount = newWords.size,
                        message = message
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

    private val _showImportDialog = MutableStateFlow(false)
    val showImportDialog: StateFlow<Boolean> = _showImportDialog.asStateFlow()

    private val _navigateToGptImport = MutableStateFlow(false)
    val navigateToGptImport: StateFlow<Boolean> = _navigateToGptImport.asStateFlow()

    fun showImportDialog() {
        _showImportDialog.value = true
    }

    fun dismissImportDialog() {
        _showImportDialog.value = false
    }

    fun onNavigateToGptImport() {
        _navigateToGptImport.value = true
    }

    fun onGptImportNavigated() {
        _navigateToGptImport.value = false
    }

    fun exportWords(context: android.content.Context, words: List<WordEntity>) {
        viewModelScope.launch {
            try {
                val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()
                val sheet = workbook.createSheet(screenTitle)

                // Header style
                val headerStyle = workbook.createCellStyle().apply {
                    val font = workbook.createFont().apply {
                        bold = true
                        fontHeightInPoints = 12
                    }
                    setFont(font)
                }

                // Header row
                val headerRow = sheet.createRow(0)
                val headers = listOf("单词", "音标", "释义", "例句", "难度", "状态")
                headers.forEachIndexed { index, title ->
                    headerRow.createCell(index).apply {
                        setCellValue(title)
                        cellStyle = headerStyle
                    }
                }

                // Data rows
                words.forEachIndexed { index, word ->
                    val row = sheet.createRow(index + 1)
                    row.createCell(0).setCellValue(word.word)
                    row.createCell(1).setCellValue(word.phonetic ?: "")
                    row.createCell(2).setCellValue(word.meaning)
                    row.createCell(3).setCellValue(word.example ?: "")
                    row.createCell(4).setCellValue(word.difficulty.toDouble())
                    row.createCell(5).setCellValue(if (word.isLearned) "已学" else "未学")
                }

                // Auto-size columns
                headers.indices.forEach { sheet.setColumnWidth(it, 5000) }
                sheet.setColumnWidth(0, 4000)  // 单词
                sheet.setColumnWidth(2, 8000)  // 释义
                sheet.setColumnWidth(3, 10000) // 例句

                // Write to cache file
                val fileName = "${screenTitle}_${java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.getDefault()).format(java.util.Date())}.xlsx"
                val file = java.io.File(context.cacheDir, fileName)
                java.io.FileOutputStream(file).use { fos ->
                    workbook.write(fos)
                }
                workbook.close()

                // Share via FileProvider
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(android.content.Intent.createChooser(intent, "导出${screenTitle}"))

                _importResult.value = ImportResult(
                    success = true,
                    message = "已导出 ${words.size} 个单词"
                )
            } catch (e: Exception) {
                _importResult.value = ImportResult(
                    success = false,
                    message = "导出失败: ${e.message}"
                )
            }
        }
    }
}
