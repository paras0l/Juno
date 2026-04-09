package com.juno.app.data.local

import android.content.Context
import com.juno.app.data.local.dao.GrammarLessonDao
import com.juno.app.data.local.dao.GrammarStageDao
import com.juno.app.data.local.entity.GrammarLessonEntity
import com.juno.app.data.local.entity.GrammarStageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrammarDataInitializer @Inject constructor(
    private val context: Context,
    private val grammarStageDao: GrammarStageDao,
    private val grammarLessonDao: GrammarLessonDao
) {
    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        val stageCount = grammarStageDao.getStageCount()
        if (stageCount == 0) {
            loadFromAssets()
        }
    }

    private suspend fun loadFromAssets() {
        try {
            val json = context.assets.open("grammar/grammar_data.json")
                .bufferedReader()
                .use { it.readText() }

            val root = JSONObject(json)

            // Parse and insert stages
            val stagesArray = root.getJSONArray("stages")
            val stages = mutableListOf<GrammarStageEntity>()
            for (i in 0 until stagesArray.length()) {
                val obj = stagesArray.getJSONObject(i)
                stages.add(
                    GrammarStageEntity(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        level = obj.getString("level"),
                        order = obj.getInt("order"),
                        totalLessons = obj.getInt("totalLessons"),
                        completedLessons = 0,
                        isUnlocked = obj.getBoolean("isUnlocked"),
                        completedAt = null
                    )
                )
            }
            grammarStageDao.insertStages(stages)

            // Parse and insert lessons
            val lessonsArray = root.getJSONArray("lessons")
            val lessons = mutableListOf<GrammarLessonEntity>()
            for (i in 0 until lessonsArray.length()) {
                val obj = lessonsArray.getJSONObject(i)
                lessons.add(
                    GrammarLessonEntity(
                        id = obj.getLong("id"),
                        stageId = obj.getLong("stageId"),
                        title = obj.getString("title"),
                        order = obj.getInt("order"),
                        content = obj.getString("content"),
                        examples = obj.getString("examples"),
                        exercises = obj.getString("exercises"),
                        isCompleted = false,
                        correctRate = 0f,
                        lastPracticedAt = null
                    )
                )
            }
            grammarLessonDao.insertLessons(lessons)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
