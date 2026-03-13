package com.juno.app.data.repository

import com.juno.app.data.local.dao.StoryDao
import com.juno.app.data.local.entity.StoryEntity
import com.juno.app.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryRepositoryImpl @Inject constructor(
    private val storyDao: StoryDao
) : StoryRepository {

    override fun getAllStories(): Flow<List<StoryEntity>> {
        return storyDao.getAllStories()
    }

    override fun getStoryById(id: Long): Flow<StoryEntity?> {
        return storyDao.getStoryByIdFlow(id)
    }

    override suspend fun getStoryByIdSync(id: Long): StoryEntity? {
        return storyDao.getStoryById(id)
    }

    override fun getStoriesByLevel(level: Int): Flow<List<StoryEntity>> {
        return storyDao.getStoriesByLevel(level)
    }

    override fun getUncompletedStories(limit: Int): Flow<List<StoryEntity>> {
        return storyDao.getUncompletedStories(limit)
    }

    override fun getCompletedStories(): Flow<List<StoryEntity>> {
        return storyDao.getCompletedStories()
    }

    override fun getCompletedStoriesCount(): Flow<Int> {
        return storyDao.getCompletedStoriesCount()
    }

    override fun searchStories(query: String): Flow<List<StoryEntity>> {
        return storyDao.searchStories(query)
    }

    override fun getStoriesForReading(maxLevel: Int, limit: Int): Flow<List<StoryEntity>> {
        return storyDao.getStoriesForReading(maxLevel, limit)
    }

    override suspend fun insertStory(story: StoryEntity): Long {
        return storyDao.insertStory(story)
    }

    override suspend fun insertStories(stories: List<StoryEntity>) {
        storyDao.insertStories(stories)
    }

    override suspend fun updateStory(story: StoryEntity) {
        storyDao.updateStory(story)
    }

    override suspend fun deleteStory(story: StoryEntity) {
        storyDao.deleteStory(story)
    }

    override suspend fun updateCompletionStatus(storyId: Long, isCompleted: Boolean) {
        storyDao.updateCompletionStatus(storyId, isCompleted)
    }

    override suspend fun markStoryAsRead(storyId: Long) {
        storyDao.markStoryAsRead(storyId)
    }

    override suspend fun deleteAllStories() {
        storyDao.deleteAllStories()
    }
}
