package com.juno.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val summary: String? = null,
    val level: Int = 1, // 1-5 difficulty level
    val style: String = "adventure", // adventure, mystery, science_fiction, romance, fairy_tale
    val wordCount: Int = 0,
    val words: String? = null, // JSON array of word IDs used
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val isRead: Boolean = false,
    val readingTimeMinutes: Int = 0
)
