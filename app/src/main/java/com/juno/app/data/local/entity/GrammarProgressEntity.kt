package com.juno.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grammar_progress")
data class GrammarProgressEntity(
    @PrimaryKey
    val id: Long = 1,
    val currentStageId: Long,  // 当前进行中的阶段
    val currentLessonId: Long,  // 当前进行中的关卡
    val totalStudyTime: Long = 0,  // 总学习时间（秒）
    val lastStudyDate: Long = System.currentTimeMillis()  // 上次学习日期
)
