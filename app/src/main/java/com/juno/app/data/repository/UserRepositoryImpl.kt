package com.juno.app.data.repository

import com.juno.app.data.local.dao.UserProgressDao
import com.juno.app.data.local.entity.UserProgressEntity
import com.juno.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userProgressDao: UserProgressDao
) : UserRepository {

    override fun getUserProgress(): Flow<UserProgressEntity?> {
        return userProgressDao.getUserProgress()
    }

    override suspend fun getUserProgressSync(): UserProgressEntity? {
        return userProgressDao.getUserProgressSync()
    }

    override suspend fun initializeProgress() {
        val existing = userProgressDao.getUserProgressSync()
        if (existing == null) {
            userProgressDao.insertProgress(UserProgressEntity())
        } else {
            // Always check if daily stats need reset when app opens
            checkAndResetDailyStats()
        }
    }

    override suspend fun incrementWordsLearned() {
        ensureProgressExists()
        checkAndResetDailyStats()
        userProgressDao.incrementWordsLearned()
    }

    override suspend fun incrementWordsReviewed() {
        ensureProgressExists()
        checkAndResetDailyStats()
        userProgressDao.incrementWordsReviewed()
    }

    override suspend fun updateStreak(streak: Int) {
        ensureProgressExists()
        userProgressDao.updateStreak(streak)
    }

    override suspend fun updateLongestStreak(streak: Int) {
        ensureProgressExists()
        userProgressDao.updateLongestStreak(streak)
    }

    override suspend fun updateDailyGoal(goal: Int) {
        ensureProgressExists()
        userProgressDao.updateDailyGoal(goal)
    }

    override suspend fun incrementMasteredWords() {
        ensureProgressExists()
        userProgressDao.incrementMasteredWords()
    }

    override suspend fun incrementCompletedStories() {
        ensureProgressExists()
        userProgressDao.incrementCompletedStories()
    }

    override suspend fun addStudyTime(minutes: Int) {
        ensureProgressExists()
        userProgressDao.addStudyTime(minutes)
    }

    override suspend fun resetDailyStatsIfNeeded() {
        checkAndResetDailyStats()
    }

    override fun isDailyGoalMet(): Flow<Boolean?> {
        return userProgressDao.isDailyGoalMet()
    }

    private suspend fun ensureProgressExists() {
        val existing = userProgressDao.getUserProgressSync()
        if (existing == null) {
            userProgressDao.insertProgress(UserProgressEntity())
        }
    }

    private suspend fun checkAndResetDailyStats() {
        val progress = userProgressDao.getUserProgressSync() ?: return
        val lastStudyDate = progress.lastStudyDate

        // If never studied before, just set today's date and ensure counters are clean
        if (lastStudyDate == null) {
            userProgressDao.resetDailyStats()
            return
        }

        val lastStudyCalendar = Calendar.getInstance().apply { timeInMillis = lastStudyDate }
        val todayCalendar = Calendar.getInstance()

        val lastStudyDay = lastStudyCalendar.get(Calendar.DAY_OF_YEAR)
        val lastStudyYear = lastStudyCalendar.get(Calendar.YEAR)
        val todayDay = todayCalendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = todayCalendar.get(Calendar.YEAR)

        if (lastStudyDay != todayDay || lastStudyYear != todayYear) {
            val daysDiff = calculateDaysDifference(lastStudyDate, System.currentTimeMillis())

            val newStreak = when {
                daysDiff == 1 -> progress.currentStreak + 1
                daysDiff > 1 -> 1
                else -> progress.currentStreak
            }

            userProgressDao.resetDailyStats()
            userProgressDao.updateStreak(newStreak)

            if (newStreak > progress.longestStreak) {
                userProgressDao.updateLongestStreak(newStreak)
            }
        }
    }

    private fun calculateDaysDifference(startTime: Long, endTime: Long): Int {
        val millisPerDay = 24 * 60 * 60 * 1000L
        return ((endTime - startTime) / millisPerDay).toInt()
    }
}
