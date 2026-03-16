package com.juno.app.domain.repository

import com.juno.app.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun getAllWords(): Flow<List<WordEntity>>
    fun getWordById(id: Long): Flow<WordEntity?>
    suspend fun getWordByIdSync(id: Long): WordEntity?
    suspend fun getWordByText(wordText: String): WordEntity?
    fun searchWords(query: String): Flow<List<WordEntity>>
    fun searchLearnedWords(query: String): Flow<List<WordEntity>>
    fun searchMasteredWords(query: String): Flow<List<WordEntity>>
    fun getUnlearnedWords(limit: Int): Flow<List<WordEntity>>
    fun getLearnedWords(): Flow<List<WordEntity>>
    fun getMasteredWords(): Flow<List<WordEntity>>
    fun getWordsByCategory(category: String): Flow<List<WordEntity>>
    fun getWordsForLearning(maxDifficulty: Int, limit: Int): Flow<List<WordEntity>>
    fun getLearnedWordsCount(): Flow<Int>
    suspend fun insertWord(word: WordEntity): Long
    suspend fun insertWords(words: List<WordEntity>)
    suspend fun updateWord(word: WordEntity)
    suspend fun deleteWord(word: WordEntity)
    suspend fun updateLearnedStatus(wordId: Long, isLearned: Boolean)
    suspend fun updateLastStudiedDate(wordId: Long, date: Long)
    fun getRecentlyStudiedWords(limit: Int): Flow<List<WordEntity>>
    suspend fun deleteAllWords()
    suspend fun getAllWordsList(): List<String>
}
