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
        val prefs = context.getSharedPreferences("juno_prefs", Context.MODE_PRIVATE)
        val currentVersion = prefs.getInt("grammar_data_version", 0)
        // Increment this target version whenever grammar_data.json is updated
        val TARGET_VERSION = 1

        if (currentVersion < TARGET_VERSION) {
            loadFromAssets()
            prefs.edit().putInt("grammar_data_version", TARGET_VERSION).apply()
        }
    }

    private suspend fun loadFromAssets() {
        try {
            val json = context.assets.open("grammar/grammar_data.json")
                .bufferedReader()
                .use { it.readText() }

            val root = JSONObject(json)

            // Parse and sync stages
            val stagesArray = root.getJSONArray("stages")
            for (i in 0 until stagesArray.length()) {
                val obj = stagesArray.getJSONObject(i)
                val id = obj.getLong("id")
                val name = obj.getString("name")
                val level = obj.getString("level")
                val order = obj.getInt("order")
                val totalLessons = obj.getInt("totalLessons")
                
                val existing = grammarStageDao.getStageById(id)
                if (existing == null) {
                    grammarStageDao.insertStage(
                        GrammarStageEntity(
                            id = id,
                            name = name,
                            level = level,
                            order = order,
                            totalLessons = totalLessons,
                            completedLessons = 0,
                            isUnlocked = obj.getBoolean("isUnlocked"),
                            completedAt = null
                        )
                    )
                } else {
                    grammarStageDao.updateStaticData(id, name, level, order, totalLessons)
                }
            }

            // Parse and sync lessons
            val lessonsArray = root.getJSONArray("lessons")
            for (i in 0 until lessonsArray.length()) {
                val obj = lessonsArray.getJSONObject(i)
                val id = obj.getLong("id")
                val stageId = obj.getLong("stageId")
                val title = obj.getString("title")
                val order = obj.getInt("order")
                val content = obj.getString("content")
                val examples = obj.getString("examples")
                val exercises = obj.getString("exercises")

                val existing = grammarLessonDao.getLessonById(id)
                if (existing == null) {
                    grammarLessonDao.insertLesson(
                        GrammarLessonEntity(
                            id = id,
                            stageId = stageId,
                            title = title,
                            order = order,
                            content = content,
                            examples = examples,
                            exercises = exercises,
                            isCompleted = false,
                            correctRate = 0f,
                            lastPracticedAt = null
                        )
                    )
                } else {
                    grammarLessonDao.updateStaticData(id, title, order, content, examples, exercises)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
