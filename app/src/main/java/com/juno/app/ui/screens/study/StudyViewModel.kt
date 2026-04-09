package com.juno.app.ui.screens.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.GrammarDataInitializer
import com.juno.app.data.local.dao.GrammarStageDao
import com.juno.app.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudyUiState(
    val todayWordCount: Int = 15,
    val grammarStageCount: Int = 6,
    val grammarCompletedStages: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val grammarStageDao: GrammarStageDao,
    private val grammarDataInitializer: GrammarDataInitializer
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            grammarDataInitializer.initializeIfNeeded()
            loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val unlearnedWords = wordRepository.getUnlearnedWords(15).first()
                val stages = grammarStageDao.getAllStages().first()
                val completedStages = stages.count { it.completedLessons >= it.totalLessons }

                _uiState.value = StudyUiState(
                    todayWordCount = unlearnedWords.size.coerceAtLeast(1),
                    grammarStageCount = stages.size,
                    grammarCompletedStages = completedStages,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
