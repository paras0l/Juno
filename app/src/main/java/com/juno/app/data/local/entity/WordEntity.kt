package com.juno.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val phonetic: String? = null,
    val meaning: String,
    val example: String? = null,
    val translation: String? = null,
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val difficulty: Int = 1, // 1-5 scale
    val isLearned: Boolean = false,
    val lastStudiedDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val category: String? = null,
    val tags: String? = null // Comma-separated tags
)
