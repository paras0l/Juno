package com.juno.app.data.repository

import com.juno.app.data.local.dao.WordDao
import com.juno.app.data.local.entity.WordEntity
import com.juno.app.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao
) : WordRepository {

    override fun getAllWords(): Flow<List<WordEntity>> {
        return wordDao.getAllWords()
    }

    override fun getWordById(id: Long): Flow<WordEntity?> {
        return wordDao.getWordByIdFlow(id)
    }

    override suspend fun getWordByIdSync(id: Long): WordEntity? {
        return wordDao.getWordById(id)
    }

    override suspend fun getWordByText(wordText: String): WordEntity? {
        return wordDao.getWordByText(wordText)
    }

    override fun searchWords(query: String): Flow<List<WordEntity>> {
        return wordDao.searchWords(query)
    }

    override fun searchLearnedWords(query: String): Flow<List<WordEntity>> {
        return wordDao.searchLearnedWords(query)
    }

    override fun searchMasteredWords(query: String): Flow<List<WordEntity>> {
        return wordDao.searchMasteredWords(query)
    }

    override fun getUnlearnedWords(limit: Int): Flow<List<WordEntity>> {
        return wordDao.getUnlearnedWords(limit)
    }

    override fun getLearnedWords(): Flow<List<WordEntity>> {
        return wordDao.getLearnedWords()
    }

    override fun getMasteredWords(): Flow<List<WordEntity>> {
        return wordDao.getMasteredWords()
    }

    override fun getWordsByCategory(category: String): Flow<List<WordEntity>> {
        return wordDao.getWordsByCategory(category)
    }

    override fun getWordsForLearning(maxDifficulty: Int, limit: Int): Flow<List<WordEntity>> {
        return wordDao.getWordsForLearning(maxDifficulty, limit)
    }

    override fun getLearnedWordsCount(): Flow<Int> {
        return wordDao.getLearnedWordsCount()
    }

    override suspend fun insertWord(word: WordEntity): Long {
        return wordDao.insertWord(word)
    }

    override suspend fun insertWords(words: List<WordEntity>) {
        wordDao.insertWords(words)
    }

    override suspend fun updateWord(word: WordEntity) {
        wordDao.updateWord(word)
    }

    override suspend fun deleteWord(word: WordEntity) {
        wordDao.deleteWord(word)
    }

    override suspend fun updateLearnedStatus(wordId: Long, isLearned: Boolean) {
        wordDao.updateLearnedStatus(wordId, isLearned)
    }

    override suspend fun updateLastStudiedDate(wordId: Long, date: Long) {
        wordDao.updateLastStudiedDate(wordId, date)
    }

    override fun getRecentlyStudiedWords(limit: Int): Flow<List<WordEntity>> {
        return wordDao.getRecentlyStudiedWords(limit)
    }

    override suspend fun deleteAllWords() {
        wordDao.deleteAllWords()
    }

    override suspend fun getAllWordsList(): List<String> {
        return wordDao.getAllWordsList()
    }
}
