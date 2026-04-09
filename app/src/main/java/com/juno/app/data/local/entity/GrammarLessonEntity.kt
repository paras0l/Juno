package com.juno.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grammar_lessons")
data class GrammarLessonEntity(
    @PrimaryKey
    val id: Long,
    val stageId: Long,         // 所属阶段ID
    val title: String,         // "五大基本句型"
    val order: Int,            // 本阶段内的顺序
    val content: String,      // 语法点讲解（Markdown文本）
    val examples: String,     // 例句（JSON数组）
    val exercises: String,    // 练习题（JSON数组）
    val isCompleted: Boolean = false, // 是否完成
    val correctRate: Float = 0f,    // 正确率
    val lastPracticedAt: Long? = null // 上次练习时间
)
