package com.juno.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grammar_stages")
data class GrammarStageEntity(
    @PrimaryKey
    val id: Long,
    val name: String,           // "第一阶段：构建句子骨架"
    val level: String,         // "初级·入门"
    val order: Int,            // 1-6
    val totalLessons: Int,      // 总关卡数
    val completedLessons: Int, // 已完成关卡数
    val isUnlocked: Boolean,   // 是否解锁
    val completedAt: Long? = null  // 完成时间戳
)
