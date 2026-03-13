package com.juno.app.domain.repository

import com.juno.app.data.local.entity.ReviewRecordEntity
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun getAllReviewRecords(): Flow<List<ReviewRecordEntity>>
    fun getDueReviews(): Flow<List<ReviewRecordEntity>>
    fun getDueReviewsLimited(limit: Int): Flow<List<ReviewRecordEntity>>
    fun getDueReviewsCount(): Flow<Int>
    fun getLearningReviews(): Flow<List<ReviewRecordEntity>>
    fun getMasteredReviews(): Flow<List<ReviewRecordEntity>>
    fun getMasteredCount(): Flow<Int>
    fun getReviewRecordByWordId(wordId: Long): Flow<ReviewRecordEntity?>
    suspend fun getReviewRecordByWordIdSync(wordId: Long): ReviewRecordEntity?
    suspend fun getReviewRecordById(id: Long): ReviewRecordEntity?
    suspend fun insertReviewRecord(record: ReviewRecordEntity): Long
    suspend fun updateReviewRecord(record: ReviewRecordEntity)
    suspend fun deleteReviewRecord(record: ReviewRecordEntity)
    suspend fun deleteReviewRecordByWordId(wordId: Long)
    suspend fun deleteAllReviewRecords()
    suspend fun processReview(wordId: Long, quality: Int): ReviewRecordEntity
    fun getDueReviewsByDifficulty(maxDifficulty: Int, limit: Int): Flow<List<ReviewRecordEntity>>
}
