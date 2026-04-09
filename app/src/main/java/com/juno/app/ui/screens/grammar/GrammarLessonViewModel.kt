package com.juno.app.ui.screens.grammar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juno.app.data.local.dao.GrammarLessonDao
import com.juno.app.data.local.dao.GrammarProgressDao
import com.juno.app.data.local.dao.GrammarStageDao
import com.juno.app.data.local.entity.GrammarLessonEntity
import com.juno.app.data.local.entity.GrammarProgressEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

data class Exercise(
    val id: Int,
    val question: String,
    val options: List<String>,
    val answer: Int,
    val explanation: String
)

data class ExerciseResult(
    val total: Int,
    val correct: Int,
    val answers: Map<Int, Int>,       // exerciseId -> selected
    val correctAnswers: Map<Int, Int>  // exerciseId -> correct
) {
    val scoreRate: Float get() = if (total == 0) 0f else correct.toFloat() / total
}

enum class LessonStep { CONTENT, EXERCISES, RESULT }

data class GrammarLessonUiState(
    val lesson: GrammarLessonEntity? = null,
    val exercises: List<Exercise> = emptyList(),
    val step: LessonStep = LessonStep.CONTENT,
    val answers: Map<Int, Int> = emptyMap(), // exerciseId -> selected option index
    val result: ExerciseResult? = null,
    val isLoading: Boolean = true,
    val nextLessonId: Long? = null
)

@HiltViewModel
class GrammarLessonViewModel @Inject constructor(
    private val grammarLessonDao: GrammarLessonDao,
    private val grammarProgressDao: GrammarProgressDao,
    private val grammarStageDao: GrammarStageDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrammarLessonUiState())
    val uiState: StateFlow<GrammarLessonUiState> = _uiState.asStateFlow()

    fun loadLesson(lessonId: Long) {
        viewModelScope.launch {
            val lesson = grammarLessonDao.getLessonById(lessonId)
            if (lesson != null) {
                val exercises = parseExercises(lesson.exercises)
                val nextLesson = grammarLessonDao.getNextLesson(lesson.stageId, lesson.order)
                _uiState.value = GrammarLessonUiState(
                    lesson = lesson,
                    exercises = exercises,
                    step = LessonStep.CONTENT,
                    answers = emptyMap(),
                    result = null,
                    isLoading = false,
                    nextLessonId = nextLesson?.id
                )
                // Update progress
                grammarProgressDao.updateProgress(
                    GrammarProgressEntity(
                        currentStageId = lesson.stageId,
                        currentLessonId = lessonId,
                        lastStudyDate = System.currentTimeMillis()
                    )
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun proceedToExercises() {
        _uiState.value = _uiState.value.copy(step = LessonStep.EXERCISES)
    }

    fun selectAnswer(exerciseId: Int, optionIndex: Int) {
        val current = _uiState.value.answers.toMutableMap()
        current[exerciseId] = optionIndex
        _uiState.value = _uiState.value.copy(answers = current)
    }

    fun submitLesson() {
        viewModelScope.launch {
            val exercises = _uiState.value.exercises
            val answers = _uiState.value.answers
            val correctAnswers = exercises.associate { it.id to it.answer }

            val correct = exercises.count { ex -> answers[ex.id] == ex.answer }
            val result = ExerciseResult(
                total = exercises.size,
                correct = correct,
                answers = answers,
                correctAnswers = correctAnswers
            )

            val lesson = _uiState.value.lesson ?: return@launch
            val correctRate = result.scoreRate

            // Mark lesson as completed and save correctRate
            val updatedLesson = lesson.copy(
                isCompleted = true,
                correctRate = correctRate,
                lastPracticedAt = System.currentTimeMillis()
            )
            grammarLessonDao.updateLesson(updatedLesson)

            // Update stage progress
            val allLessons = grammarLessonDao.getLessonsByStageSync(lesson.stageId)
            val completedCount = allLessons.count { it.isCompleted } + if (!lesson.isCompleted) 1 else 0
            val stage = grammarStageDao.getStageById(lesson.stageId)
            if (stage != null) {
                val isStageCompleted = completedCount >= stage.totalLessons
                val updatedStage = stage.copy(
                    completedLessons = completedCount,
                    completedAt = if (isStageCompleted) System.currentTimeMillis() else stage.completedAt
                )
                grammarStageDao.updateStage(updatedStage)

                // Unlock next stage if this one is completed
                if (isStageCompleted) {
                    val nextStage = grammarStageDao.getStageById(stage.id + 1)
                    if (nextStage != null && !nextStage.isUnlocked) {
                        grammarStageDao.updateStage(nextStage.copy(isUnlocked = true))
                    }
                }
            }

            _uiState.value = _uiState.value.copy(
                step = LessonStep.RESULT,
                result = result,
                lesson = updatedLesson
            )
        }
    }

    fun retryLesson() {
        _uiState.value = _uiState.value.copy(
            step = LessonStep.CONTENT,
            answers = emptyMap(),
            result = null
        )
    }

    private fun parseExercises(exercisesJson: String): List<Exercise> {
        return try {
            val arr = JSONArray(exercisesJson)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val optionsArr = obj.getJSONArray("options")
                val options = (0 until optionsArr.length()).map { optionsArr.getString(it) }
                Exercise(
                    id = obj.getInt("id"),
                    question = obj.getString("question"),
                    options = options,
                    answer = obj.getInt("answer"),
                    explanation = obj.getString("explanation")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
