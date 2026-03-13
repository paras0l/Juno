package com.juno.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey
    val id: Int = 1, // Single row for user progress
    val totalWordsLearned: Int = 0,
    val totalWordsReviewed: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val dailyGoal: Int = 10,
    val wordsLearnedToday: Int = 0,
    val wordsReviewedToday: Int = 0,
    val lastStudyDate: Long? = null,
    val totalStudyTimeMinutes: Int = 0,
    val completedStories: Int = 0,
    val masteredWords: Int = 0
)
