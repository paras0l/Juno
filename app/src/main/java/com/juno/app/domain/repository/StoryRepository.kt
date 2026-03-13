package com.juno.app.domain.repository

import com.juno.app.data.local.entity.StoryEntity
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    fun getAllStories(): Flow<List<StoryEntity>>
    fun getStoryById(id: Long): Flow<StoryEntity?>
    suspend fun getStoryByIdSync(id: Long): StoryEntity?
    fun getStoriesByLevel(level: Int): Flow<List<StoryEntity>>
    fun getUncompletedStories(limit: Int): Flow<List<StoryEntity>>
    fun getCompletedStories(): Flow<List<StoryEntity>>
    fun getCompletedStoriesCount(): Flow<Int>
    fun searchStories(query: String): Flow<List<StoryEntity>>
    fun getStoriesForReading(maxLevel: Int, limit: Int): Flow<List<StoryEntity>>
    suspend fun insertStory(story: StoryEntity): Long
    suspend fun insertStories(stories: List<StoryEntity>)
    suspend fun updateStory(story: StoryEntity)
    suspend fun deleteStory(story: StoryEntity)
    suspend fun updateCompletionStatus(storyId: Long, isCompleted: Boolean)
    suspend fun markStoryAsRead(storyId: Long)
    suspend fun deleteAllStories()
}
