package com.juno.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val phonetic: String? = null,
    val definitions: String? = null,
    val sentence: String? = null,
    val sentenceTranslation: String? = null,
    val etymology: String? = null,
    val collocations: String? = null,
    val gptContent: String? = null,
    val difficulty: Int = 1,
    val isLearned: Boolean = false,
    val lastStudiedDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val category: String? = null,
    val tags: String? = null
)
