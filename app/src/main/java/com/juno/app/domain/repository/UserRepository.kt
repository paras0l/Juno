package com.juno.app.domain.repository

import com.juno.app.data.local.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProgress(): Flow<UserProgressEntity?>
    suspend fun getUserProgressSync(): UserProgressEntity?
    suspend fun initializeProgress()
    suspend fun incrementWordsLearned()
    suspend fun incrementWordsReviewed()
    suspend fun updateStreak(streak: Int)
    suspend fun updateLongestStreak(streak: Int)
    suspend fun updateDailyGoal(goal: Int)
    suspend fun incrementMasteredWords()
    suspend fun incrementCompletedStories()
    suspend fun addStudyTime(minutes: Int)
    suspend fun resetDailyStatsIfNeeded()
    fun isDailyGoalMet(): Flow<Boolean?>
}
