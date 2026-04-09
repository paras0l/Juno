package com.juno.app.ui.screens.grammar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.GrammarDataInitializer
import com.juno.app.data.local.dao.GrammarLessonDao
import com.juno.app.data.local.dao.GrammarStageDao
import com.juno.app.data.local.entity.GrammarLessonEntity
import com.juno.app.data.local.entity.GrammarStageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GrammarUiState(
    val stages: List<GrammarStageEntity> = emptyList(),
    val selectedStageId: Long? = null,
    val lessons: List<GrammarLessonEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isLessonsLoading: Boolean = false
)

@HiltViewModel
class GrammarViewModel @Inject constructor(
    private val grammarStageDao: GrammarStageDao,
    private val grammarLessonDao: GrammarLessonDao,
    private val grammarDataInitializer: GrammarDataInitializer
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrammarUiState())
    val uiState: StateFlow<GrammarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Ensure data is inserted before we start collecting
            grammarDataInitializer.initializeIfNeeded()
            loadStages()
        }
    }

    private fun loadStages() {
        viewModelScope.launch {
            grammarStageDao.getAllStages().collect { stages ->
                _uiState.value = _uiState.value.copy(
                    stages = stages,
                    isLoading = false
                )
            }
        }
    }

    fun loadLessonsForStage(stageId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLessonsLoading = true, selectedStageId = stageId)
            grammarLessonDao.getLessonsByStage(stageId).collect { lessons ->
                _uiState.value = _uiState.value.copy(
                    lessons = lessons,
                    isLessonsLoading = false
                )
            }
        }
    }

    fun getFirstUncompletedLesson(stageId: Long): Long? {
        val stage = _uiState.value.stages.find { it.id == stageId }
        if (stage?.isUnlocked == false) return null
        val lessons = _uiState.value.lessons.filter { it.stageId == stageId }
        return lessons.firstOrNull { !it.isCompleted }?.id ?: lessons.firstOrNull()?.id
    }
}

