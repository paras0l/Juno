package com.juno.app.data.repository

import com.juno.app.data.local.dao.ReviewDao
import com.juno.app.data.local.entity.ReviewRecordEntity
import com.juno.app.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val reviewDao: ReviewDao
) : ReviewRepository {

    companion object {
        private const val MIN_EASE_FACTOR = 1.3f
        private const val INITIAL_EASE_FACTOR = 2.5f
        private const val MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000L
    }

    override fun getAllReviewRecords(): Flow<List<ReviewRecordEntity>> {
        return reviewDao.getAllReviewRecords()
    }

    override fun getDueReviews(): Flow<List<ReviewRecordEntity>> {
        return reviewDao.getDueReviews()
    }

    override fun getDueReviewsLimited(limit: Int): Flow<List<ReviewRecordEntity>> {
        return reviewDao.getDueReviewsLimited(limit = limit)
    }

    override fun getDueReviewsCount(): Flow<Int> {
        return reviewDao.getDueReviewsCount()
    }

    override fun getLearningReviews(): Flow<List<ReviewRecordEntity>> {
        return reviewDao.getLearningReviews()
    }

    override fun getMasteredReviews(): Flow<List<ReviewRecordEntity>> {
        return reviewDao.getMasteredReviews()
    }

    override fun getMasteredCount(): Flow<Int> {
        return reviewDao.getMasteredCount()
    }

    override fun getReviewRecordByWordId(wordId: Long): Flow<ReviewRecordEntity?> {
        return reviewDao.getReviewRecordByWordIdFlow(wordId)
    }

    override suspend fun getReviewRecordByWordIdSync(wordId: Long): ReviewRecordEntity? {
        return reviewDao.getReviewRecordByWordId(wordId)
    }

    override suspend fun getReviewRecordById(id: Long): ReviewRecordEntity? {
        return reviewDao.getReviewRecordById(id)
    }

    override suspend fun insertReviewRecord(record: ReviewRecordEntity): Long {
        return reviewDao.insertReviewRecord(record)
    }

    override suspend fun updateReviewRecord(record: ReviewRecordEntity) {
        reviewDao.updateReviewRecord(record)
    }

    override suspend fun deleteReviewRecord(record: ReviewRecordEntity) {
        reviewDao.deleteReviewRecord(record)
    }

    override suspend fun deleteReviewRecordByWordId(wordId: Long) {
        reviewDao.deleteReviewRecordByWordId(wordId)
    }

    override suspend fun deleteAllReviewRecords() {
        reviewDao.deleteAllReviewRecords()
    }

    override fun getDueReviewsByDifficulty(maxDifficulty: Int, limit: Int): Flow<List<ReviewRecordEntity>> {
        return reviewDao.getDueReviewsByDifficulty(maxDifficulty, System.currentTimeMillis(), limit)
    }

    override suspend fun processReview(wordId: Long, quality: Int): ReviewRecordEntity {
        val currentTime = System.currentTimeMillis()
        val existingRecord = reviewDao.getReviewRecordByWordId(wordId)

        return if (existingRecord != null) {
            val updatedRecord = calculateSM2(existingRecord, quality, currentTime)
            reviewDao.updateReviewRecord(updatedRecord)
            updatedRecord
        } else {
            val newRecord = ReviewRecordEntity(
                wordId = wordId,
                easeFactor = INITIAL_EASE_FACTOR,
                interval = 0,
                repetitions = 0,
                nextReviewDate = currentTime,
                lastReviewDate = currentTime,
                totalReviews = 1,
                correctReviews = if (quality >= 3) 1 else 0
            )
            val calculatedRecord = calculateSM2(newRecord, quality, currentTime)
            val id = reviewDao.insertReviewRecord(calculatedRecord)
            calculatedRecord.copy(id = id)
        }
    }

    private fun calculateSM2(record: ReviewRecordEntity, quality: Int, currentTime: Long): ReviewRecordEntity {
        val q = quality.coerceIn(0, 5)

        var newEaseFactor = record.easeFactor + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f))
        newEaseFactor = max(MIN_EASE_FACTOR, newEaseFactor)

        val newRepetitions: Int
        val newInterval: Int

        if (q < 3) {
            newRepetitions = 0
            newInterval = 1
        } else {
            newRepetitions = record.repetitions + 1
            newInterval = when (newRepetitions) {
                1 -> 1
                2 -> 6
                else -> (record.interval * newEaseFactor).toInt()
            }
        }

        val nextReviewDate = currentTime + (newInterval * MILLISECONDS_PER_DAY)

        return record.copy(
            easeFactor = newEaseFactor,
            interval = newInterval,
            repetitions = newRepetitions,
            nextReviewDate = nextReviewDate,
            lastReviewDate = currentTime,
            totalReviews = record.totalReviews + 1,
            correctReviews = if (q >= 3) record.correctReviews + 1 else record.correctReviews
        )
    }
}
