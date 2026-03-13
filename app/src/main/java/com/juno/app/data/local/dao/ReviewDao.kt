package com.juno.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.juno.app.data.local.entity.ReviewRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewRecord(record: ReviewRecordEntity): Long

    @Update
    suspend fun updateReviewRecord(record: ReviewRecordEntity)

    @Delete
    suspend fun deleteReviewRecord(record: ReviewRecordEntity)

    @Query("SELECT * FROM review_records WHERE id = :id")
    suspend fun getReviewRecordById(id: Long): ReviewRecordEntity?

    @Query("SELECT * FROM review_records WHERE wordId = :wordId")
    suspend fun getReviewRecordByWordId(wordId: Long): ReviewRecordEntity?

    @Query("SELECT * FROM review_records WHERE wordId = :wordId")
    fun getReviewRecordByWordIdFlow(wordId: Long): Flow<ReviewRecordEntity?>

    @Query("SELECT * FROM review_records WHERE nextReviewDate <= :currentTime ORDER BY nextReviewDate ASC")
    fun getDueReviews(currentTime: Long = System.currentTimeMillis()): Flow<List<ReviewRecordEntity>>

    @Query("SELECT * FROM review_records WHERE nextReviewDate <= :currentTime ORDER BY nextReviewDate ASC LIMIT :limit")
    fun getDueReviewsLimited(currentTime: Long = System.currentTimeMillis(), limit: Int): Flow<List<ReviewRecordEntity>>

    @Query("SELECT COUNT(*) FROM review_records WHERE nextReviewDate <= :currentTime")
    fun getDueReviewsCount(currentTime: Long = System.currentTimeMillis()): Flow<Int>

    @Query("SELECT * FROM review_records ORDER BY lastReviewDate DESC")
    fun getAllReviewRecords(): Flow<List<ReviewRecordEntity>>

    @Query("SELECT * FROM review_records WHERE repetitions > 0 ORDER BY nextReviewDate ASC")
    fun getLearningReviews(): Flow<List<ReviewRecordEntity>>

    @Query("SELECT * FROM review_records WHERE repetitions >= 3")
    fun getMasteredReviews(): Flow<List<ReviewRecordEntity>>

    @Query("SELECT COUNT(*) FROM review_records WHERE repetitions >= 3")
    fun getMasteredCount(): Flow<Int>

    @Query("DELETE FROM review_records WHERE wordId = :wordId")
    suspend fun deleteReviewRecordByWordId(wordId: Long)

    @Query("DELETE FROM review_records")
    suspend fun deleteAllReviewRecords()

    @Query("""
        SELECT wr.* FROM review_records wr
        INNER JOIN words w ON wr.wordId = w.id
        WHERE w.difficulty <= :maxDifficulty
        AND wr.nextReviewDate <= :currentTime
        ORDER BY wr.nextReviewDate ASC
        LIMIT :limit
    """)
    fun getDueReviewsByDifficulty(maxDifficulty: Int, currentTime: Long, limit: Int): Flow<List<ReviewRecordEntity>>
}
