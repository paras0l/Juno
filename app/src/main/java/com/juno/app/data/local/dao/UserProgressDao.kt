package com.juno.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.juno.app.data.local.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: UserProgressEntity)

    @Update
    suspend fun updateProgress(progress: UserProgressEntity)

    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getUserProgress(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun getUserProgressSync(): UserProgressEntity?

    @Query("UPDATE user_progress SET totalWordsLearned = totalWordsLearned + 1, wordsLearnedToday = wordsLearnedToday + 1 WHERE id = 1")
    suspend fun incrementWordsLearned()

    @Query("UPDATE user_progress SET totalWordsReviewed = totalWordsReviewed + 1, wordsReviewedToday = wordsReviewedToday + 1 WHERE id = 1")
    suspend fun incrementWordsReviewed()

    @Query("UPDATE user_progress SET currentStreak = :streak WHERE id = 1")
    suspend fun updateStreak(streak: Int)

    @Query("UPDATE user_progress SET longestStreak = :streak WHERE id = 1")
    suspend fun updateLongestStreak(streak: Int)

    @Query("UPDATE user_progress SET dailyGoal = :goal WHERE id = 1")
    suspend fun updateDailyGoal(goal: Int)

    @Query("UPDATE user_progress SET masteredWords = masteredWords + 1 WHERE id = 1")
    suspend fun incrementMasteredWords()

    @Query("UPDATE user_progress SET completedStories = completedStories + 1 WHERE id = 1")
    suspend fun incrementCompletedStories()

    @Query("UPDATE user_progress SET totalStudyTimeMinutes = totalStudyTimeMinutes + :minutes WHERE id = 1")
    suspend fun addStudyTime(minutes: Int)

    @Query("UPDATE user_progress SET wordsLearnedToday = 0, wordsReviewedToday = 0, lastStudyDate = :date WHERE id = 1")
    suspend fun resetDailyStats(date: Long = System.currentTimeMillis())

    @Query("SELECT wordsLearnedToday >= dailyGoal FROM user_progress WHERE id = 1")
    fun isDailyGoalMet(): Flow<Boolean?>
}
