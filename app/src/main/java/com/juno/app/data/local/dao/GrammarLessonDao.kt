package com.juno.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.juno.app.data.local.entity.GrammarLessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrammarLessonDao {
    @Query("SELECT * FROM grammar_lessons WHERE stageId = :stageId ORDER BY `order`")
    fun getLessonsByStage(stageId: Long): Flow<List<GrammarLessonEntity>>

    @Query("SELECT * FROM grammar_lessons WHERE id = :id")
    suspend fun getLessonById(id: Long): GrammarLessonEntity?

    @Query("SELECT * FROM grammar_lessons WHERE stageId = :stageId ORDER BY `order`")
    suspend fun getLessonsByStageSync(stageId: Long): List<GrammarLessonEntity>

    @Query("SELECT * FROM grammar_lessons WHERE stageId = :stageId AND `order` > :currentOrder ORDER BY `order` LIMIT 1")
    suspend fun getNextLesson(stageId: Long, currentOrder: Int): GrammarLessonEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLesson(lesson: GrammarLessonEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLessons(lessons: List<GrammarLessonEntity>)

    @Update
    suspend fun updateLesson(lesson: GrammarLessonEntity)

    @Query("UPDATE grammar_lessons SET title = :title, `order` = :order, content = :content, examples = :examples, exercises = :exercises WHERE id = :id")
    suspend fun updateStaticData(id: Long, title: String, order: Int, content: String, examples: String, exercises: String)

    @Query("SELECT COUNT(*) FROM grammar_lessons")
    suspend fun getLessonCount(): Int
}
