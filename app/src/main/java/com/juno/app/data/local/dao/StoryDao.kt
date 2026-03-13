package com.juno.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.juno.app.data.local.entity.StoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Update
    suspend fun updateStory(story: StoryEntity)

    @Delete
    suspend fun deleteStory(story: StoryEntity)

    @Query("SELECT * FROM stories WHERE id = :id")
    suspend fun getStoryById(id: Long): StoryEntity?

    @Query("SELECT * FROM stories WHERE id = :id")
    fun getStoryByIdFlow(id: Long): Flow<StoryEntity?>

    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE level = :level ORDER BY createdAt DESC")
    fun getStoriesByLevel(level: Int): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE isCompleted = 0 ORDER BY RANDOM() LIMIT :limit")
    fun getUncompletedStories(limit: Int): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedStories(): Flow<List<StoryEntity>>

    @Query("SELECT COUNT(*) FROM stories WHERE isCompleted = 1")
    fun getCompletedStoriesCount(): Flow<Int>

    @Query("UPDATE stories SET isCompleted = :isCompleted WHERE id = :storyId")
    suspend fun updateCompletionStatus(storyId: Long, isCompleted: Boolean)

    @Query("UPDATE stories SET isRead = 1 WHERE id = :storyId")
    suspend fun markStoryAsRead(storyId: Long)

    @Query("SELECT * FROM stories WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchStories(query: String): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE level <= :maxLevel ORDER BY RANDOM() LIMIT :limit")
    fun getStoriesForReading(maxLevel: Int, limit: Int): Flow<List<StoryEntity>>

    @Query("DELETE FROM stories")
    suspend fun deleteAllStories()
}
